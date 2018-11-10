function faqer_query(client, message) {
    var id_parts = client.id.split('-');
    var chat_id = id_parts[id_parts.length - 1];
    $http.get($global.constants.faqerUrl, {
        query: {
            q:message,
            chat_id: chat_id,
            message_id: client.message_id,
            dialogue_id: chat_id
        },
        dataType: "json"
    })
        .then(function (data) {
            // var id = data.results[0].document_id;
            var answer = data.hypothesis[0].answer;
            var score = data.hypothesis[0].answerId;

            if (score > threshold) {
                //cut link from answer
                var words = answer.split(" ");
                var link;
                var button_exists = 0;
                for (var i = 0; i < words.length; i++) {
                    if (words[i].indexOf("http") > -1) { //check that word begins from http
                        link = words[i];
                        answer = answer.replace(words[i], "");
                        button_exists = 1;
                    }
                }

                //add link to button
                if (button_exists == 1) {
                    $reactions.buttons([{
                        button: {
                            text: "Ссылка",
                            url: link,
                            hide: false
                        }
                    }]);
                }
                $reactions.answer(answer);
                $reactions.transition("/");
            } else {
                $reactions.answer("Извините, я не нашёл ответ!");
            }
        })
        .catch(function (response, status, error) {
            $reactions.answer($global.errors.faqer);
            $reactions.transition("/");
        });
}