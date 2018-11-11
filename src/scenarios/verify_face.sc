theme:/
    state: verify
        a: Пришлите вашу фотографию для верификации.

        state: 
            event: imageEvent
            event: fileEvent
            script: 
                var images = $request.rawRequest.message.photo;
                $client.image = images[images.length - 1].file_id;
                log(JSON.stringify($request.data.eventData));
                $http.post('http://89.223.27.150:9001/verify_face', {
                    dataType : 'application/json',
                    body : {
                        "chat_id": $client.id,
                        "image_id": $client.image
                    },
                    headers : {"content-type": "application/json;charset=utf-8"},
                })
                .then(function (data) {
                    data = JSON.parse(data);
                    log(JSON.stringify(data));
                    if(data.is_matches_chat_id === true){
                        $client.verified = true;
                        $reactions.answer("Верификация пройдена.");
                        $reactions.transition($client.prev_state);
                    } else {
                        $reactions.answer("Не удалось пройти верификацию. Попробуйте ещё раз.");
                        $reactions.transition("..");
                    }
                })
                .catch(function (response, status, error) {
                    $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                    $reactions.answer(JSON.stringify(error));
                    $reactions.transition("..");
                });
            
        
        state: catch
            q: *
            a: Простите, я вас не понял. Пришлите вашу фотографию для верификации.
            go!: ..
        


