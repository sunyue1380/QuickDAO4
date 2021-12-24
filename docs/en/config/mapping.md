# Entity class mapping to table

An example to show how entity class mapping to database table.

```
└── cn.schoolwow.quickdao.entity
    ├── User.java
    ├── PlayHistory.java
    └── user
        ├── User.java
        └── history
            └── LoginHistory.java
```

After mapping

|entity class|table name|
|---|---|
|cn.schoolwow.quickdao.entity.User|user|
|cn.schoolwow.quickdao.entity.PlayHistory|play_history|
|cn.schoolwow.quickdao.entity.user.User|user@user|
|cn.schoolwow.quickdao.entity.user.history.LoginHistory|user_history@login_history|

> Attention! If you use packageName("cn.schoolwow.quickdao.entity","t") then following table names will add ``t`` prefix.