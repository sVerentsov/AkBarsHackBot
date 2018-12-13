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
            $http.post('http://bugulma.eora.ru:9779/check_user_exists', {
                                dataType : 'application/json',
                                body : {
                                    "user_id": $client.id
                                },
                                headers : {"content-type": "application/json;charset=utf-8"},
                            })
                            .then(function (data) {
                                data = JSON.parse(data);
                                $client.user_exists = data.user_exists;
                                if ($client.user_exists){
                                    $reactions.transition("/start/auth");
                                } else {
                                    $reactions.transition("/start/signup");    
                                }
                            })
                            .catch(function (response, status, error) {
                                $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                                $reactions.answer(JSON.stringify(error));
                                $reactions.transition("..");
                                $client.user_exists = false;
                            });

        state: signup
            a: Вы первый раз в этом боте. Пришлите ваши данные авторизации: одно видео или аудиозаписи вашего голоса (файлом, не голосовым сообщением)

            state: load_file
                event: telegramAnyMessage
                script: 
                    var file;
                    if ("video" in $request.rawRequest.message) {
                        file = $request.rawRequest.message.video;
                        $client.type = "video";
                    } else {
                        file = $request.rawRequest.message.audio;
                        $client.type = "audio";
                    }
                    $client.file_id = file.file_id;
                    log(JSON.stringify($request.data.eventData));
                    $http.post('http://bugulma.eora.ru:9779/add_reference_file', {
                        dataType : 'application/json',
                        body : {
                            "user_id": $client.id,
                            "type": $client.type,
                            "file_id": $client.file_id
                        },
                        headers : {"content-type": "application/json;charset=utf-8"},
                    })
                    .then(function (data) {
                        data = JSON.parse(data);
                        if(data.success == true) {
                            $reactions.answer("Отлично! Добавьте ещё записи голоса или нажмите /start, чтобы попробовать авторизоваться!");
                        } else {
                            $reactions.answer(JSON.stringify(data));
                        }
                    })
                    .catch(function (response, status, error) {
                        $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                        $reactions.answer(JSON.stringify(error));
                        $reactions.transition("..");
                    });

        state: auth
            a: Пройдите авторизацию! Загрузите видео или запись голоса (файлом, а не голосовым сообщением)

            state: load_file
                event: telegramAnyMessage
                script: 
                    var file;
                    if ("video" in $request.rawRequest.message) {
                        file = $request.rawRequest.message.video;
                        $client.type = "video";
                    } else {
                        file = $request.rawRequest.message.audio;
                        $client.type = "audio";
                    }
                    $client.file_id = file.file_id;
                    $http.post('http://bugulma.eora.ru:9779/authenticate', {
                        dataType : 'application/json',
                        body : {
                            "user_id": $client.id,
                            "type": $client.type,
                            "file_id": $client.file_id
                        },
                        headers : {"content-type": "application/json;charset=utf-8"},
                    })
                    .then(function (data) {
                        data = JSON.parse(data);
                        if(data.success == true) {
                            $reactions.answer("Привет, " + $request.rawRequest.message.from.first_name + "! Нажмите /start, чтобы попробовать ещё раз");
                        } else {
                            if("error" in data)
                            {
                                $reactions.answer(data['error']);
                            }
                            $reactions.answer("Авторизация не удалась. Попробуйте ещё раз.");
                            $reactions.buttons("Зарегистрироваться заново");
                        }

                    })
                    .catch(function (response, status, error) {
                        $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                        $reactions.answer(JSON.stringify(error));
                        $reactions.transition("..");
                    });

    state: reset
        q: * Зарегистрироваться заново *
        script:
            $http.post('http://bugulma.eora.ru:9779/restart', {
                                    dataType : 'application/json',
                                    body : {
                                        "user_id": $client.id
                                    },
                                    headers : {"content-type": "application/json;charset=utf-8"},
                                })
                                .then(function (data) {
                                    data = JSON.parse(data);
                                    if(data.success == true) {
                                        $reactions.answer("Ваши данные для авторизации стёрты.");
                                        $reactions.transition("/start/signup");
                                    } else {
                                        $reactions.answer("Что-то пошло не так");
                                        $reactions.answer(JSON.stringify(data));
                                    }
                                })
                                .catch(function (response, status, error) {
                                    $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                                    $reactions.answer(JSON.stringify(error));
                                    $reactions.transition("..");
                                    $client.user_exists = false;
                                });