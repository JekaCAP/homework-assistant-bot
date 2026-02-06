-- Вставка тестовых курсов

-- Курс 1: Java Basics
INSERT INTO courses (code, name, description, icon, sort_order, difficulty_level)
VALUES
    ('java-basics', 'Java Basics',
     'Основы программирования на Java. Изучите синтаксис, ООП и базовые алгоритмы.',
     '☕', 1, 'BEGINNER')
    ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
                              description = EXCLUDED.description,
                              icon = EXCLUDED.icon,
                              sort_order = EXCLUDED.sort_order,
                              difficulty_level = EXCLUDED.difficulty_level,
                              updated_at = CURRENT_TIMESTAMP;

-- Курс 2: Spring Boot
INSERT INTO courses (code, name, description, icon, sort_order, difficulty_level)
VALUES
    ('spring-boot', 'Spring Boot',
     'Создание современных веб-приложений на Spring Boot. REST API, Security, Data JPA.',
     '🌱', 2, 'INTERMEDIATE')
    ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
                              description = EXCLUDED.description,
                              icon = EXCLUDED.icon,
                              sort_order = EXCLUDED.sort_order,
                              difficulty_level = EXCLUDED.difficulty_level,
                              updated_at = CURRENT_TIMESTAMP;

-- Курс 3: Databases
INSERT INTO courses (code, name, description, icon, sort_order, difficulty_level)
VALUES
    ('databases', 'Databases',
     'Работа с реляционными базами данных. SQL, PostgreSQL, миграции, оптимизация запросов.',
     '🗄️', 3, 'INTERMEDIATE')
    ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
                              description = EXCLUDED.description,
                              icon = EXCLUDED.icon,
                              sort_order = EXCLUDED.sort_order,
                              difficulty_level = EXCLUDED.difficulty_level,
                              updated_at = CURRENT_TIMESTAMP;

-- Курс 4: Docker & DevOps
INSERT INTO courses (code, name, description, icon, sort_order, difficulty_level)
VALUES
    ('docker-devops', 'Docker & DevOps',
     'Контейнеризация приложений, Docker, Docker Compose, CI/CD пайплайны.',
     '🐳', 4, 'ADVANCED')
    ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
                              description = EXCLUDED.description,
                              icon = EXCLUDED.icon,
                              sort_order = EXCLUDED.sort_order,
                              difficulty_level = EXCLUDED.difficulty_level,
                              updated_at = CURRENT_TIMESTAMP;

-- Курс 5: Algorithms & Data Structures
INSERT INTO courses (code, name, description, icon, sort_order, difficulty_level)
VALUES
    ('algorithms', 'Algorithms & Data Structures',
     'Сложные алгоритмы и структуры данных. Подготовка к техническим собеседованиям.',
     '🧠', 5, 'ADVANCED')
    ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
                              description = EXCLUDED.description,
                              icon = EXCLUDED.icon,
                              sort_order = EXCLUDED.sort_order,
                              difficulty_level = EXCLUDED.difficulty_level,
                              updated_at = CURRENT_TIMESTAMP;

-- Вставка заданий для курса Java Basics
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 1, 'Hello World', 'Создайте свою первую программу на Java, которая выводит "Hello, World!"', 100, 2, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'java-basics'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 2, 'Калькулятор', 'Реализуйте простой калькулятор с базовыми арифметическими операциями', 100, 4, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'java-basics'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 3, 'Управление потоком', 'Используйте условные операторы и циклы для решения задач', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'java-basics'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Вставка заданий для курса Spring Boot
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 1, 'REST API', 'Создайте простое REST API для управления сущностями', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'spring-boot'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 2, 'Spring Security', 'Добавьте аутентификацию и авторизацию в приложение', 100, 10, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'spring-boot'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 3, 'Spring Data JPA', 'Реализуйте работу с базой данных через JPA репозитории', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'spring-boot'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Вставка заданий для курса Databases
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 1, 'SQL запросы', 'Напишите сложные SQL запросы с JOIN, GROUP BY, подзапросами', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'databases'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 2, 'Оптимизация запросов', 'Проанализируйте и оптимизируйте медленные SQL запросы', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'databases'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 3, 'Транзакции и блокировки', 'Реализуйте работу с транзакциями и разберитесь с уровнями изоляции', 100, 10, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'databases'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Вставка тестового админа
INSERT INTO admins (telegram_id, telegram_username, full_name, email, role, is_active)
VALUES
    (759144172, 'admin_user', 'Администратор Системы', 'admin@example.com', 'ADMIN', TRUE)
    ON CONFLICT (telegram_id) DO UPDATE SET
    telegram_username = EXCLUDED.telegram_username,
                                     full_name = EXCLUDED.full_name,
                                     email = EXCLUDED.email,
                                     role = EXCLUDED.role,
                                     is_active = EXCLUDED.is_active,
                                     updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 1, 'Dockerfile', 'Создайте Dockerfile для упаковки простого веб-приложения', 100, 4, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'docker-devops'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 2, 'Docker Compose', 'Настройте multi-container приложение с помощью Docker Compose', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'docker-devops'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 3, 'CI/CD Pipeline', 'Настройте базовый CI/CD pipeline с тестами и деплоем', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'docker-devops'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 4, 'Мониторинг и логи', 'Настройте сбор логов и мониторинг для контейнеризированного приложения', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'docker-devops'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Дополнительные задания для курса Algorithms & Data Structures
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 1, 'Сортировки', 'Реализуйте различные алгоритмы сортировки и сравните их эффективность', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'algorithms'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 2, 'Деревья и графы', 'Реализуйте основные алгоритмы обхода деревьев и графов', 100, 10, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'algorithms'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 3, 'Динамическое программирование', 'Решите классические задачи динамического программирования', 100, 12, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'algorithms'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 4, 'Хеш-таблицы', 'Реализуйте свою хеш-таблицу и решите задачи с использованием хеширования', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'algorithms'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Дополнительные задания для курса Java Basics (чтобы было больше 3 заданий)
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 4, 'Коллекции', 'Работа с ArrayList, HashMap и другими коллекциями Java', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'java-basics'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 5, 'Исключения', 'Обработка исключений и создание пользовательских исключений', 100, 4, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'java-basics'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Дополнительные задания для курса Databases
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 4, 'Индексы', 'Создание и оптимизация индексов для улучшения производительности', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'databases'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 5, 'Нормализация', 'Применение нормальных форм к проектированию базы данных', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'databases'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Дополнительные задания для курса Spring Boot
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 4, 'Тестирование', 'Написание unit и integration тестов для Spring Boot приложения', 100, 8, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'spring-boot'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 5, 'Кэширование', 'Реализация кэширования данных для улучшения производительности', 100, 6, 'HOMEWORK', TRUE
FROM courses c WHERE c.code = 'spring-boot'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Добавление заданий типа TEST для всех курсов
INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 100, 'Финальный тест', 'Итоговый тест по всему курсу', 100, 2, 'TEST', TRUE
FROM courses c WHERE c.code = 'java-basics'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 100, 'Финальный тест', 'Итоговый тест по всему курсу', 100, 2, 'TEST', TRUE
FROM courses c WHERE c.code = 'spring-boot'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 100, 'Финальный тест', 'Итоговый тест по всему курсу', 100, 2, 'TEST', TRUE
FROM courses c WHERE c.code = 'databases'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 100, 'Финальный тест', 'Итоговый тест по всему курсу', 100, 2, 'TEST', TRUE
FROM courses c WHERE c.code = 'docker-devops'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

INSERT INTO assignments (course_id, number, title, description, max_score, estimated_hours, type, is_active)
SELECT c.id, 100, 'Финальный тест', 'Итоговый тест по всему курсу', 100, 2, 'TEST', TRUE
FROM courses c WHERE c.code = 'algorithms'
    ON CONFLICT (course_id, number) DO UPDATE SET
    title = EXCLUDED.title,
                                           description = EXCLUDED.description,
                                           max_score = EXCLUDED.max_score,
                                           estimated_hours = EXCLUDED.estimated_hours,
                                           type = EXCLUDED.type,
                                           is_active = EXCLUDED.is_active,
                                           updated_at = CURRENT_TIMESTAMP;

-- Вывод информации о созданных данных
SELECT 'Курсы и задания успешно созданы!' as message;