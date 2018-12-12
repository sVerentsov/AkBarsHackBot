require: ../functions/auth_apis.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

require: verify_face.sc

patterns: 
    $Yes = [ну] [конечно|всё|все|вроде|пожалуй|возможно] (да|даа|lf|ага|точно|угу|верно|ок|ok|окей|окай|okay|оке|именно|подтвержд*|йес) [да|конечно|конешно|канешна|всё|все|вроде|пожалуй|возможно]
    $No = (нет|неат|ниат|неа|ноуп|ноу|найн) [нет] [спасибо]

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте | в начало ) *   
        script:
            $client.id = $request.channelUserId;
            var $client.user_exists = check_user_exists($client.id);
        if: $client.user_exists == true
            a: Пройдите авторизацию! Загрузите фото или запись голоса (файлом, а не голосовым сообщением)
            go!: /start/auth
        else: 
            go!: /start/signup

        state: signup
            a: Вы первый раз в этом боте. Пришлите ваши данные авторизации: одно видео или аудиозаписи вашего голоса (файлом, не голосовым сообщением)

            state: load_file
                event: telegramAnyMessage
                script: 
                    var file;
                    if ("video" in $request.rawRequest.message) {
                        file = $request.rawRequest.message.video;
                    } else {
                        file = $request.rawRequest.message.audio;
                    }
                    $client.file_id = file.file_id;
                    log(JSON.stringify($request.data.eventData));
                    $http.post('http://bugulma.eora.ru:9779/add_reference_file', {
                        dataType : 'application/json',
                        body : {
                            "user_id": $client.id,
                            "file_id": $client.file_id
                        },
                        headers : {"content-type": "application/json;charset=utf-8"},
                    })
                    .then(function (data) {
                        $reactions.answer("Отлично! Добавьте ещё записи голоса или нажмите /start, чтобы попробовать авторизоваться!");
                    })
                    .catch(function (response, status, error) {
                        $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                        $reactions.answer(JSON.stringify(error));
                        $reactions.transition("..");
                    });

        state: auth
            a: Пройдите авторизацию! Загрузите фото или запись голоса (файлом, а не голосовым сообщением)

            state: load_file
                event: telegramAnyMessage
                script: 
                    var file;
                    if ("video" in $request.rawRequest.message) {
                        file = $request.rawRequest.message.video;
                    } else {
                        file = $request.rawRequest.message.audio;
                    }
                    $client.file_id = file.file_id;
                    $http.post('http://bugulma.eora.ru:9779/authenticate', {
                        dataType : 'application/json',
                        body : {
                            "user_id": $client.id,
                            "file_id": $client.file_id
                        },
                        headers : {"content-type": "application/json;charset=utf-8"},
                    })
                    .then(function (data) {
                        if(data.success) {
                            $reactions.answer("Привет, " + $request.rawRequest.message.from.first_name + "! Нажмите /start, чтобы попробовать ещё раз");
                        } else {
                            $reactions.answer("Это не вы. Попробуйте ещё раз.");
                        }

                    })
                    .catch(function (response, status, error) {
                        $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                        $reactions.answer(JSON.stringify(error));
                        $reactions.transition("..");
                    });

