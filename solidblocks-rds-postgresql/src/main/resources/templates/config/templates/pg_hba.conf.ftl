# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             127.0.0.1/32            trust
local   all             all                                     trust
host    all             all             ::1/128                 trust
host    all             ${USERNAME}     0.0.0.0/0               md5
