require: ../functions/faqer.js
require: ../dictionaries/constants.yaml
    var = constants
require: ../dictionaries/errors.yaml
    var = errors

theme: /
    state: start
        q: * ( *start | ping | привет | здравствуйте) *   
        a: Здравствуйте! Чем я могу вам помочь?
        go!: /faq

    state: faq
        q!: *
        script:
            faqer_query($client,$parseTree.text);
        go!: /