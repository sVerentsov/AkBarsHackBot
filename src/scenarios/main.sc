require: ../functions/faqer.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

require: card.sc
require: address.sc
require: verify_face.sc
require: receipt.sc

patterns: 
    $Yes = [ну] [конечно|всё|все|вроде|пожалуй|возможно] (да|даа|lf|ага|точно|угу|верно|ок|ok|окей|окай|okay|оке|именно|подтвержд*|йес) [да|конечно|конешно|канешна|всё|все|вроде|пожалуй|возможно]
    $No = (нет|неат|ниат|неа|ноуп|ноу|найн) [нет] [спасибо]

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте | в начало ) *   
        script:
            $client.id = $request.channelUserId;
            var user_exists = check_user_exists($client.id);
        if: user_exists == true
            a: Пройдите авторизацию! Загрузите фото или запись голоса (файлом, а не голосовым сообщением)
            go!: /start/auth
        else: 
            go!: /start/signup

        state: signup
            a: Вы первый раз в этом боте. Пришлите ваши данные авторизации: одно видео или аудиозаписи вашего голоса (файлом, не голосовым сообщением)

            state: load_file
                event: fileEvent
                script: 
                    var images = $request.rawRequest.message.photo;
                    $client.image = images[images.length - 1].file_id;
                    log(JSON.stringify($request.data.eventData));
                    $http.post('http://89.223.27.150:9001/add_reference_file', {
                        dataType : 'application/json',
                        body : {
                            "user_id": $client.id,
                            "file_id": $client.image
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
                event: fileEvent
                script: 
                    var images = $request.rawRequest.message.photo;
                    $client.image = images[images.length - 1].file_id;
                    log(JSON.stringify($request.data.eventData));
                    $http.post('http://89.223.27.150:9001/authenticate', {
                        dataType : 'application/json',
                        body : {
                            "user_id": $client.id,
                            "file_id": $client.image
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

