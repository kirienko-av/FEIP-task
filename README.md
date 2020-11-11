# FEIP-task
## Пример создания простого санитайзера:
```
Simple simple = Sanitizer.of(CORRECT, Simple.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .get();
```
Для добавления поддержки нового типа данных необходимо создать класс расширяющий BaseType.
