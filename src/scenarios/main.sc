require: ../functions/faqer.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте) *   
        script:
            $client.id = $request.channelUserId;
        a: Здравствуйте! Чем я могу вам помочь?
        go!: /faq

    state: faq
        q!: *
        script:
            $client.message_id = $request.rawRequest.message.message_id;
            faqer_query($client,$parseTree.text);
        go!: /