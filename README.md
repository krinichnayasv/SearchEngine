##Индексация заданных сайтов. Выполнение поисковых запросов по сайтам.
____
###Используемые технологии: Russian Morphology, Spring, Sql, ForkJoin, Thread, SnakeYaml 
____
###Дополнительная информация:
> Из папки lib нужно подключить все файлы jar.

> Все основные методы запуска в классе ParseSiteOrPage
 
> Внедрение переменной bean-класса DBMethods в ParseSiteOrPage выполнено через SpringContext
____
###Настройки SQL
> 1. Создать базу.
- CREATE DATABASE search_engine;

> 2. Создать таблицу site.

- create table site
  (id integer not null auto_increment,
  last_error varchar(255),
  name varchar(255) not null,
  status enum ('INDEXING','INDEXED','FAILED') NOT NULL,
  status_time bigint not null,
  url varchar(255) not null,
  primary key (id));

> 3. Создать таблицу lemma.

- create table lemma
  (id integer not null auto_increment,
  lemma varchar(255),
  frequency integer not null,
  site_id integer not null,
  primary key (id),
  UNIQUE KEY unique_SL(id, site_id));

- alter table lemma add constraint Site_for_lemma foreign key (site_id)
  references site (id) on delete cascade on update cascade;

> 4. Создать таблицу page.

- create table `page`
  (id integer not null auto_increment,
  code integer not null,
  content mediumtext,
  path text,
  site_id integer not null,
  primary key (id),
  UNIQUE KEY unique_SP(id, site_id));

- CREATE INDEX index_path ON `page` (path(1000)) USING BTREE;

- alter table page add constraint Site_for_page foreign key (site_id)
  references site (id) on delete cascade on update cascade;

> 5. Создать таблицу index и ключи.

- create table `index`
  (id integer not null auto_increment,
  lemma_id integer not null,
  page_id integer not null,
  `rank` float not null,
  primary key (id)) ;

- alter table `index` add constraint Page_for_index foreign key (page_id)
  references `page` (id) on delete cascade on update cascade;
- alter table `index` add constraint Lemma_for_index foreign key (lemma_id)
  references lemma (id) on delete cascade on update cascade;

> 6. Создать таблицу field и вставить данные

- create table field
  (id integer not null auto_increment,
  name varchar(255) not null,
  selector varchar(255) not null,
  weight float not null);

- INSERT INTO field VALUES(1, 'title', 'title', 1);
- INSERT INTO field VALUES(2, 'body', 'body', 0.8);
____
###Файл application.yml парсится с помощью технологии SnakeYaml и должен содержать:
> 1. Подключение к базе MySQL и настройки:
- spring.datasource:
    - url: jdbc:mysql://localhost:3306/search_engine
    - username: root
    - password: Svetlana77
- spring.jpa.hibernate:
    - ddl-auto: update
    - show-sql: true

> 2. User agent и Referrer
- По умолчанию:
- properties:
    - userAgent: Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36
    - referrer: http://www.google.com
- можно указать свои варианты

> 3. Список сайтов:
- Пример:
- properties:
    - sites:
        - url: http://www.test.ru (ОБЯЗАТЕЛЬНО! В конце не надо ставить слэш!)
        - name: Имя сайта
        - url: http://www.primer.ru
        - name: Пример
____
####Работа программы
> После запуска, программа работает по ссылке http://localhost:8080/
> автоматом переходит на http://localhost:8080/statistics/
#####Вкладки
> Вкладка DASHBOARD - Общая информация по индексации, данных в БД и сайтам отдельно

> Вкладка MANAGEMENT - Запуск / Остановка индексации, проиндексировать отдельную страницу (ОБЯЗАТЕЛЬНО! указывать в конце слэш), проиндексировать отдельно сайт (ОБЯЗАТЕЛЬНО! НЕ указывать в конце слэш, как в файле application.yml).

> Вкладка SEARCH - Поиск информации по страницам выбранного сайта / всех сайтов.
___