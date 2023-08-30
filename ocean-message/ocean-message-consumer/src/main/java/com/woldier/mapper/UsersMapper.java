package com.woldier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.woldier.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description user_mapper
 *
 * @author: woldier wong
 * @date: 2023/8/27$ 15:54$
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    List<Users> getUsers(@Param("table_name") String tableName,
                        @Param("start_index") int startIndex,
                        @Param("limit_num") int limitNum);


    long count(@Param("table_name") String tableName);
}
