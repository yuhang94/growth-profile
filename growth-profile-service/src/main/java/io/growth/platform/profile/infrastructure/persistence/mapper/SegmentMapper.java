package io.growth.platform.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.growth.platform.profile.infrastructure.persistence.dataobject.SegmentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SegmentMapper extends BaseMapper<SegmentDO> {
}
