patterns:
    $CardNumber = $regexp<(\d{16}|\d{4} \d{4} \d{4} \d{4})>

theme:/
    state: card
        q: * перев* * карт* *
        a: Пришлите фотографию карты или её номер.

        state: 
            event: imageEvent
            event: fileEvent
            script: 
                $client.image = $request.data.eventData.url;
                $http.post('http://89.223.27.150:9001/get_card_number', {
                    dataType : 'application/json',
                    body : {
                        "image_link":"https://i.imgur.com/3ESMQIh.png"
                    },
                    headers : {"content-type": "application/json;charset=utf-8"},
                })
                .then(function (data) {
                    data = JSON.parse(data);
                    log(JSON.stringify(data));
                    if(data.success === true){
                        $client.card = data.card_number;
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
                a: Хорошо, перевод на карту {{$client.card}} совершён.
                go!: /
            
        
        state: catch
            q: *
            a: Простите, я вас не понял. Пришлите фотографию карты или её номер в формате 0000 0000 0000 0000.
            go!: ..
        


