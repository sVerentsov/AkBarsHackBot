require: ../functions/faqer.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

require: card.sc
require: address.sc

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте) *   
        script:
            $client.address = -1;
            $client.service = "Ак+барс+банк+банкомат";
            $client.X = 0;
            $client.id = $request.channelUserId;
        a: Здравствуйте! Чем я могу вам помочь?
        go!: /

    state: faq
        q!: *
        script:
            $client.message_id = $request.rawRequest.message.message_id;
            faqer_query($client, $parseTree.text);
        go!: /