patterns:
    $CardNumber = $regexp<(\d{16}|\d{4} \d{4} \d{4} \d{4})>

theme:/
    state: card
        q: * перев* * карт* *
        if: $request.channelType != 'telegram'
            a: Чтобы перевести деньги на другую карту, воспользуйтесь нашим приложением или банкоматом. 
            go!: /
        a: Пришлите фотографию карты или её номер.

        state: 
            event: imageEvent
            event: fileEvent
            script: 
                var images = $request.rawRequest.message.photo;
                $client.image = images[images.length - 1].file_id;
                log(JSON.stringify($request.data.eventData));
                $http.post('http://89.223.27.150:9001/get_card_number', {
                    dataType : 'application/json',
                    body : {
                        "image_id": $client.image
                    },
                    headers : {"content-type": "application/json;charset=utf-8"},
                })
                .then(function (data) {
                    data = JSON.parse(data);
                    log(JSON.stringify(data));
                    if(data.success === true){
                        $client.card = data.card_number;
                        $reactions.answer("Карта " + $client.card);
                        $reactions.transition("../by_num/payment_complete");
                    } else {
                        $reactions.answer("Не удалось распознать карту, попробуйте ещё раз!");
                        $reactions.transition("..");
                    }
                })
                .catch(function (response, status, error) {
                    $reactions.answer("Сервис распознавания не отвечает, попробуйте отправить номер карты текстом");
                    $reactions.answer(JSON.stringify(error));
                    $reactions.transition("..");
                });
        
        state: by_num
            q: * $CardNumber *
            script:
                $client.card = $parseTree.CardNumber;
            go!: ./payment_complete

            state: payment_complete
                if: $client.verified === true
                    a: Хорошо, перевод на карту совершён.
                else: 
                    script: $client.prev_state = "/card/by_num/payment_complete";
                    go!: /verify
                go!: /
            
        
        state: catch
            q: *
            a: Простите, я вас не понял. Пришлите фотографию карты или её номер в формате 0000 0000 0000 0000.
            go!: ..
        


