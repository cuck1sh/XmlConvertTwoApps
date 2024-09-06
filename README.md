Первое приложение принимает xml объект через PostMapping на корневом эндпоинте.
  - Обрабатывает xml объект в json формат и сохранаяет строку в файле с названием зависящим от Data/Type, Data/Creation/Date и количеством записей внутри (прим. Information-2016-05-31-9876.log)
  - Отправляет запрос на второе приложение через брокер сообщений RabbitMQ (Облачный сервис). В сообщении брокера передает названия файла, по которому можно будет узнать тип, дату и номер орбабатываемой записи

Второе приложение находится в ожидании сообщений из брокера. Как только сообщение приходит приложение
  - Ищет по типу, дате и номеру из сообщения нужную запись в общей куче из директории /data/new, в которой собраны все записи
  - Проверяет наличие имеющегося файла в директории /data/batched
    - Если в файле из batched меньше 100 записей, оно записывает в этот файл
    - Если в файле уже 100 записей, оно создает новый файл, увеличивая его порядковый номер (прим. Information-2016-05-31-0001.log -> Information-2016-05-31-0002.log)

Засчет механики RabbitMQ, даже если второе приложение выйдет из строя и первое приложение накопит новые записи, при восстановлении работы, второе приложение начнет с той записи, на которой остановилось

Для запуска системы, нужно запустить оба приложения
