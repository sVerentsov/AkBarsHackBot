require: ../functions/faqer.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте) *   
        script:
            $client.id = $request.rawRequest.message.from.id;
        a: Здравствуйте! Чем я могу вам помочь?
        go!: /faq

    state: faq
        q!: *
        script:
            message_id = $request.rawRequest.message.message_id;
            faqer_query($client,$parseTree.text, message_id);
        go!: /