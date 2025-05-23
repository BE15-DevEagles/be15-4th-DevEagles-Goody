package com.deveagles.be15_deveagles_be.features.team.query.mapper;

import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamInformationResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamMemberResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeamMapper {
  List<MyTeamListResponse> selectMyTeamList(@Param("userId") Long userId);

  List<TeamMemberResponse> selectTeamMembers(Long teamId);

  TeamInformationResponse selectTeamInformation(@Param("teamId") Long teamId);
}
