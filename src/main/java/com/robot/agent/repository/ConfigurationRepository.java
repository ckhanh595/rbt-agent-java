package com.robot.agent.repository;

import com.robot.agent.entity.AgentConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<AgentConfigurationEntity, Long> {
    AgentConfigurationEntity findTopByOrderByCreatedAtDesc();
}
