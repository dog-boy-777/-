//package com.it.weblogclient.service.impl;
//
//
//import com.it.weblogclient.domain.App;
//import com.it.weblogclient.domain.dto.CollapseDto;
//import com.it.weblogclient.domain.dto.ErrorDetailsDto;
//import com.it.weblogclient.domain.result.Result;
//import com.it.weblogclient.domain.result.LogResult;
//import com.it.weblogclient.service.LogService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Objects;
//
///**
// * @author Administrator
// * @description 针对表【log】的数据库操作Service实现
// * @createDate 2024-10-24 09:20:36
// */
//@Service
//@Slf4j
//public class LogServiceImpl implements LogService {
//    @Autowired
//    LogRepository logRepository;
//    @Autowired
//    MongoTemplate mongoTemplate;
////    @Resource
////    LogDeque logDeque;
//    @Autowired
//    KafkaTemplate kafkaTemplate;
//
////    /**
////     * 记录日志
////     *
////     * @param myLogDto
////     */
////    @Override
////    public LogResult record(MyLogDto myLogDto, HttpServletRequest request) throws IOException {
////        // 由于MyLogDto里包含collectionName字段，并且该字段在存储时不需要存入，这里进行剔除
////
////        // 1.使用hutool包提供的复制方法进行copy
////        MyLog myLog = new MyLog();
////        BeanUtils.copyProperties(myLogDto, myLog);
////        log.info("复制后的Mylog为: {}", myLog);
////
////        // 2.存入对应集合
//////        mongoTemplate.insert(myLog,myLogDto.getCollectionName());
////        myLog.setId(HutoolUtil.getId());
////        logDeque.addLog(myLog);
//////        mongoTemplate.insert(myLog, "log");
////        return LogResult.success();
////    }
//
////    /**
////     * 上报错误信息
////     *
////     * @param errorDetailsDto
////     * @return
////     */
////    @Override
////    public Result report(ErrorDetailsDto errorDetailsDto) {
////        Long appId = errorDetailsDto.getAppId();
////        ErrorDetails errorDetails = new ErrorDetails();
////        BeanUtils.copyProperties(errorDetailsDto, errorDetails);
////
////        Query query = new Query(Criteria.where("appId").is(appId));
////        App app = mongoTemplate.findOne(query, App.class, "app");
////        mongoTemplate.insert(errorDetails, app.getName() + "_log_" + appId);
////        return Result.success();
////    }
//
//    public LogResult getByAppId(Long appid) {
//        Query query = new Query(Criteria.where("appId").is(appid));
//        List<MyLog> logs = mongoTemplate.find(query, MyLog.class, "log");
//        return LogResult.success(logs);
//    }
//
//    public LogResult deleteById(Long id) {
//        Query query = Query.query(Criteria.where("id").is(id));
//        mongoTemplate.remove(query, MyLog.class);
//        return LogResult.success();
//    }
//
//    /**
//     * 客户端上报异常
//     *
//     * @param errorDetailsDto
//     * @return
//     */
//    @Override
//    public LogResult exceptionReport(ErrorDetailsDto errorDetailsDto) {
////        // 去掉appId
////        Long appId = errorDetailsDto.getAppId();
////        ErrorDetails errorDetails = new ErrorDetails();
////        BeanUtils.copyProperties(errorDetailsDto, errorDetails);
////        log.info("上报的错误信息为{}", errorDetails);
////
////        Query query = new Query(Criteria.where("id").is(appId));
////        App app = mongoTemplate.findOne(query, App.class);
////
////        if (!Objects.isNull(app)) {
////            String collection = app.getName() + "_log_" + appId;
////            mongoTemplate.insert(errorDetails, collection);
////            return LogResult.success();
////        }
////        return LogResult.error("appId不合法");
//
//        kafkaTemplate.send("test",errorDetailsDto);
//        System.out.println("--------------------发送成功---------------------");
//        return LogResult.success();
//    }
//
//    /**
//     * 崩溃信息上报
//     *
//     * @param collapseDto
//     * @return
//     */
//    @Override
//    public Result collapseReport(CollapseDto collapseDto) {
//        // 通过appId获取appName
//        Long appId = collapseDto.getMeta().getAppId();
//        Query query = new Query(Criteria.where("id").is(appId));
//        App app = mongoTemplate.findOne(query, App.class, "app");
//
//        // 将数据存到该app对应的log集合里
//        if (!Objects.isNull(app)) {
//            String collection = app.getName() + "_log";
//            mongoTemplate.insert(collapseDto, collection);
//            return Result.success();
//        }
//        return Result.error("appId不合法");
//    }
//}
//
//
//
//
