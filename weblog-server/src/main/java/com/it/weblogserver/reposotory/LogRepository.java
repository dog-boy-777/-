package com.it.weblogserver.reposotory;


import com.it.weblogclient.domain.MyLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<MyLog,String> {
}
