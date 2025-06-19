package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.mapper.AgencyCertificationMapper;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.mapper.WorkerCertificationMapper;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务人员认证信息表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
@Service
public class AgencyCertificationAuditServiceImpl extends ServiceImpl<AgencyCertificationAuditMapper, AgencyCertificationAudit> implements IAgencyCertificationAuditService {

    @Autowired
    private AgencyCertificationMapper agentCertificationMapper;

    /**
     * 审核服务人员认证分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult<AgencyCertificationAudit> pageQuery(AgencyCertificationAuditPageQueryReqDTO dto) {
        Page<AgencyCertificationAudit> page = PageUtils.parsePageQuery(dto, AgencyCertificationAudit.class);


        Page<AgencyCertificationAudit> certificationAuditPage = baseMapper.selectPage(page,
                new LambdaQueryWrapper<AgencyCertificationAudit>()
                        .like(StringUtil.isNotEmpty(dto.getLegalPersonName()), AgencyCertificationAudit::getLegalPersonName,dto.getLegalPersonName()) // 法人姓名
                        .like(StringUtil.isNotEmpty(dto.getName()), AgencyCertificationAudit::getName, dto.getName()) //企业名称
                        .eq(dto.getAuditStatus() != null, AgencyCertificationAudit::getAuditStatus, dto.getAuditStatus()) // 审核状态，0：未审核，1：已审核
                        .eq(dto.getCertificationStatus() != null, AgencyCertificationAudit::getCertificationStatus, dto.getCertificationStatus())); // 认证状态，2：认证通过，3：认证失败

        return PageUtils.toPage(certificationAuditPage,AgencyCertificationAudit.class);
    }

    /**
     * 审核服务人员认证
     * @param id  认证id
     * @param dto
     */
    @Override
    public void audit(Long id, CertificationAuditReqDTO dto) {
        Long userId = UserContext.currentUserId();
        String name = UserContext.currentUser().getName();

        // 1.校验认证表
        AgencyCertificationAudit agencyCertificationAudit = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(agencyCertificationAudit)) {
            throw new ForbiddenOperationException("当前认证信息不存在");
        }
        AgencyCertification agencyCertification = agentCertificationMapper.selectById(agencyCertificationAudit.getServeProviderId());

        // 2.获取设置认证状态
        Integer certificationStatus = dto.getCertificationStatus();
        if ( dto.getCertificationStatus() == 2) { //认证成功
            agencyCertificationAudit.setAuditStatus(1); //审核状态-0：未审核，1：已审核',
            agencyCertificationAudit.setAuditorId(userId); //审核人id
            agencyCertificationAudit.setAuditorName(name); //审核人名称
            agencyCertificationAudit.setAuditTime(LocalDateTime.now()); //审核时间
            agencyCertificationAudit.setCertificationStatus(2); //1：认证中，2：认证成功，3认证失败'
            // 修改审核表状态
            baseMapper.updateById(agencyCertificationAudit);
            // 修改认证表状态
            agencyCertification.setCertificationStatus(2);
            agentCertificationMapper.updateById(agencyCertification);
        }
        // 认证失败
        agencyCertificationAudit.setAuditStatus(1); //审核状态-0：未审核，1：已审核',
        agencyCertificationAudit.setAuditorId(userId); //审核人id
        agencyCertificationAudit.setAuditorName(name); //审核人名称
        agencyCertificationAudit.setAuditTime(LocalDateTime.now()); //审核时间
        agencyCertificationAudit.setCertificationStatus(certificationStatus); //1：认证中，2：认证成功，3认证失败'
        agencyCertificationAudit.setRejectReason(dto.getRejectReason()); //认证失败原因
        // 修改审核表状态
        baseMapper.updateById(agencyCertificationAudit);
        // 修改认证表状态
        agencyCertification.setCertificationStatus(3);
        agentCertificationMapper.updateById(agencyCertification);
    }
}
