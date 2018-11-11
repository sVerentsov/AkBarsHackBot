    theme:/
        state: receipt
            q: * оплат* * (счёт* | счет*) *
            if: $request.channelType != 'telegram'
                a: Чтобы оплатить счёт, воспользуйтесь нашим приложением или банкоматом. 
                go!: /
            a: Пришлите фотографию счёта.

            state: 
                event: imageEvent
                event: fileEvent
                script: 
                    var images = $request.rawRequest.message.photo;
                    $client.image = images[images.length - 1].file_id;
                    log(JSON.stringify($request.data.eventData));
                    $http.post('http://89.223.27.150:9001/detect_document', {
                        dataType : 'application/json',
                        body : {
                            "image_id": $client.image,
                            "hardcode": "sasdk"
                        },
                        headers : {"content-type": "application/json;charset=utf-8"},
                    })
                    .then(function (data) {
                        data = JSON.parse(data);
                        log(JSON.stringify(data));
                        if(data.success === true){
                            $client.bank_bill_number = data.bank_bill_number;
                            $client.bill_number = data.bill_number;
                            $client.BIK_number = data.BIK_number;
                            $client.INN_provider = data.INN_provider;
                            $client.INN_buyer = data.INN_buyer;
                            $client.cost = data.cost;
                            $reactions.answer("Вот что я увидел: \n " +
                                "БИК: " + $client.BIK_number + "\n" + 
                                "Номер счёта: " + $client.bill_number + "\n" + 
                                "Номер счёта банка: " + $client.bank_bill_number + "\n" + 
                                "ИНН получателя: " + $client.INN_provider + "\n" + 
                                "ИНН плательщика: " + $client.INN_buyer + "\n" + 
                                "Итого: " + $client.cost);
                            $reactions.transition("../confirm");
                        } else {
                            $reactions.answer("Не удалось распознать счёт, попробуйте ещё раз!");
                            $reactions.transition("..");
                        }
                    })
                    .catch(function (response, status, error) {
                        $reactions.answer("Сервис распознавания не отвечает, попробуйте позже");
                        $reactions.answer(JSON.stringify(error));
                        $reactions.transition("..");
                    });
            
            state: confirm
                a: Всё верно?
                
                state: yes
                    q: * $Yes * 
                    go!: ../../payment_complete
                
                state: no
                    q: * $No *
                    a: Попробуйте ещё раз.
                    go!: ../../
                

            state: payment_complete
                if: $client.verified === true
                    a: Хорошо, счёт оплачен.
                else: 
                    script: $client.prev_state = "/receipt/payment_complete";
                    go!: /verify
                go!: /
            
            
            state: catch
                q: *
                a: Простите, я вас не понял. Пришлите фотографию счёта.
                go!: ..
            


