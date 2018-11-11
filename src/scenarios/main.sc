require: ../functions/faqer.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

require: card.sc
require: address.sc
require: verify_face.sc

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте | в начало ) *   
        script:
            $client.address = -1;
            $client.service = "Ак+барс+банк+банкомат";
            $client.X = 0;
            $client.id = $request.channelUserId);
            $client.verified = false;
        a: Здравствуйте! Чем я могу вам помочь?
        go!: /

    state: faq
        q!: *
        script:
            var id_parts = $request.questionId.split('-');
            $client.message_id = parseInt(id_parts[id_parts.length - 1], 16) % 1000000;
            faqer_query($client, $parseTree.text);
        go!: /