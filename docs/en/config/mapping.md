# Mapping entity class to database table

An example to show how entity class were mapped to database table.

```
└── cn.schoolwow.quickdao.entity
    ├── User.java
    ├── PlayHistory.java
    └── user
        ├── User.java
        └── history
            └── LoginHistory.java
```

Here's the situation in database:

|entity class|table name|
|---|---|
|cn.schoolwow.quickdao.entity.User|user|
|cn.schoolwow.quickdao.entity.PlayHistory|play_history|
|cn.schoolwow.quickdao.entity.user.User|user@user|
|cn.schoolwow.quickdao.entity.user.history.LoginHistory|user_history@login_history|

> Attention! If you use packageName("cn.schoolwow.quickdao.entity","t") then the names of foregoing table will add ``t`` prefix.