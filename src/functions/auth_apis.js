function check_user_exists(id)
{
    $http.post('http://bugulma.eora.ru:9779/check_user_exists', {
                        dataType : 'application/json',
                        body : {
                            "user_id": id
                        },
                        headers : {"content-type": "application/json;charset=utf-8"},
                    })
                    .then(function (data) {
                        data = JSON.parse(data);
                        return data.user_exists;
                    })
                    .catch(function (response, status, error) {
                        $reactions.answer("Сервис распознавания не отвечает, попробуйте позже.");
                        $reactions.answer(JSON.stringify(error));
                        $reactions.transition("..");
                        return false;
                    });
}