function faqer_query(client, message) {
    http.get($global.constants.faqerUrl, {
        query: {
            id: service_id,
            token: service_token,
            q: message
        },
        dataType: "json"
    })
        .then(function (data) {
            client.results = data.results;

            var question = data.results[0].question;
            var id = data.results[0].document_id;
            var answer = data.results[0].answer;
            var score = data.results[0].score;

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
                    reactions.buttons([{
                        button: {
                            text: "Ссылка",
                            url: link,
                            hide: false
                        }
                    }]);
                }
                reactions.transition("Next State");
            } else {
                reactions.answer("No Answer Message");
            }
        })
        .catch(function (response, status, error) {
            reactions.answer("Reason8 Error Message");
            reactions.transition("Reason8 Error State");
        });
}