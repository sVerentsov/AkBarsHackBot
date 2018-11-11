require: city/city.sc
    module = module0
    
require: address/address.sc
    module = module0

require: number/number.sc
    module = module0
    
patterns:
    $Intro = (адрес*|нахожусь|находимся|на|город*|станци*)
    
theme: /
    state: address
        q: * (снять | банкомат* ) * 
        if: $client.X == 0
            a: Назовите, пожалуйста, город и адрес вашего местоположения.
        else:
            a: Извините, я не смогла найти ваш адрес. Повторите, пожалуйста, город и адрес вашего местоположения.
        
        state: 
            q: * $Intro $City *
            script:
                $client.address = $parseTree.text.split($parseTree.Intro[0].text)[1];
            if: $parseTree.City[0].value.name == "Метро"
                go!: /city    
            go!: /search
        
        state:
            q: * $City *
            if: $parseTree.City[0].value.name == "Метро"
                script:
                    $client.address = " " + $parseTree.text;
                go!: /city
            script:
                $client.address = $parseTree.City[0].value.name + $parseTree.text.split($parseTree.City[0].text)[1];
            go!: /search
            
        state:
            q: * $Intro *
            script:
                $client.address = $parseTree.text.split($parseTree.Intro[0].text)[1];
            go!: /city

        state:
            q: *
            script:
                $client.address = " " + $parseTree.text;
            go!: /city
                
    state: city
        if: $client.X == 0
            a: В каком городе вы находитесь?
        else:
            a: Извините, я вас не поняла. Скажите, в каком вы сейчас городе?
        
        state:
            q: * $City * 
            script:
                $client.address = $parseTree.City[0].value.name + $client.address;
            go!: /search
            
        state: catch
            q: *
            script:
                $client.X = 1;
            go!: ..  
        
    state: search
        script:
            $client.address = $client.address.replace(/ /g, "+");
            
            $http.get("https://geocode-maps.yandex.ru/1.x/?format=json&geocode=" + $client.address, {timeout : 1250})
            .then(function(data) {
                if (data.response.GeoObjectCollection.featureMember.length == 0) {
                    $client.X = 1;
                    $reactions.transition("/address");
                }
                var point = JSON.stringify(data.response.GeoObjectCollection.featureMember[0].GeoObject.Point.pos).replace(" ", ",");
                point = point.replace(/"/g, "");
                
                $http.get("https://search-maps.yandex.ru/v1/?apikey=80829e94-2033-43e4-8f5f-e630bd1377b0&text=" + $client.service + "&type=biz&lang=ru_RU&ll=" + point + "&spn=0.0005,0.0005", {timeout : 1250})
                .then(function(data) {
                    $reactions.answer('Ближайший банкомат находится по адресу ' + JSON.stringify(data.features[0].properties.CompanyMetaData.address).replace(/"/g, "") + '. ');
                })
                .catch(function() {
                    $reactions.answer('Простите, произошла внутренняя ошибка, пожалуйста, перезвоните позже.');
                });
            })
            .catch(function() {
                $client.X = 1;
                $reactions.transition("/address");
            });