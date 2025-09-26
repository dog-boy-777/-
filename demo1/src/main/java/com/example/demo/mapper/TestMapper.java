package com.example.demo.mapper;

import com.example.demo.domain.TestJson;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper {

    @Insert("insert into category (mc_id, sc_id, mc_name) values (#{sentence}, #{sentence}, #{sentence});")
    public void inertTest(TestJson testJson);
}
