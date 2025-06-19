package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.mapper.AgencyCertificationMapper;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.model.dto.AgencyCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.service.IAgencyCertificationService;
import com.jzo2o.mvc.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 机构认证信息表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
@Service
public class AgencyCertificationServiceImpl extends ServiceImpl<AgencyCertificationMapper, AgencyCertification> implements IAgencyCertificationService {


    @Autowired
    private AgencyCertificationAuditMapper agencyCertificationAuditMapper;
    /**
     * 根据机构id更新
     *
     * @param agencyCertificationUpdateDTO 机构认证更新模型
     */
    @Override
    public void updateByServeProviderId(AgencyCertificationUpdateDTO agencyCertificationUpdateDTO) {
        LambdaUpdateWrapper<AgencyCertification> updateWrapper = Wrappers.<AgencyCertification>lambdaUpdate()
                .eq(AgencyCertification::getId, agencyCertificationUpdateDTO.getId())
                .set(AgencyCertification::getCertificationStatus, agencyCertificationUpdateDTO.getCertificationStatus())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getName()), AgencyCertification::getName, agencyCertificationUpdateDTO.getName())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getIdNumber()), AgencyCertification::getIdNumber, agencyCertificationUpdateDTO.getIdNumber())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getLegalPersonName()), AgencyCertification::getLegalPersonName, agencyCertificationUpdateDTO.getLegalPersonName())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getLegalPersonIdCardNo()), AgencyCertification::getLegalPersonIdCardNo, agencyCertificationUpdateDTO.getLegalPersonIdCardNo())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getBusinessLicense()), AgencyCertification::getBusinessLicense, agencyCertificationUpdateDTO.getBusinessLicense())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getCertificationTime()), AgencyCertification::getCertificationTime, agencyCertificationUpdateDTO.getCertificationTime());
        super.update(updateWrapper);
    }

    /**
     * pc新增认证申请
     * @param agencyCertificationAuditAddReqDTO
     */
    @Override
    public void postCertification(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        // 1.新增机构认证信息表
        Long userId = UserContext.currentUserId();
        AgencyCertification agencyCertification = new AgencyCertification();
        agencyCertification.setId(userId);
        agencyCertification.setCertificationStatus(1);
        agencyCertification.setCertificationTime(LocalDateTime.now());
        BeanUtil.copyProperties(agencyCertificationAuditAddReqDTO, agencyCertification);
        baseMapper.insert(agencyCertification);

        // 2.新增机构认证审核表
        AgencyCertificationAudit agencyCertificationAudit = new AgencyCertificationAudit();
        agencyCertificationAudit.setServeProviderId(agencyCertification.getId());
        agencyCertificationAudit.setName(agencyCertification.getName());
        agencyCertificationAudit.setIdNumber(agencyCertification.getIdNumber());
        agencyCertificationAudit.setLegalPersonName(agencyCertification.getLegalPersonName());
        agencyCertificationAudit.setLegalPersonIdCardNo(agencyCertification.getLegalPersonIdCardNo());
        agencyCertificationAudit.setBusinessLicense(agencyCertification.getBusinessLicense());
        agencyCertificationAudit.setAuditStatus(0);
        agencyCertificationAudit.setCertificationStatus(1);

        agencyCertificationAuditMapper.insert(agencyCertificationAudit);



    }

    /**
     * pc获取拒绝原因
     * @return
     */
    @Override
    public CertificationAuditReqDTO getRejectReason() {
        // 1.当前认证状态
        Long userId = UserContext.currentUserId();
        AgencyCertification agencyCertification = baseMapper.selectById(userId);
        if (ObjectUtil.isNull(agencyCertification)) {
            throw new ForbiddenOperationException("认证信息不存在");
        }
        // 2.认证状态
        if (agencyCertification.getCertificationStatus() == 3) {
           AgencyCertificationAudit agencyCertificationAudit = agencyCertificationAuditMapper.selectOne(new LambdaQueryWrapper<AgencyCertificationAudit>().eq(AgencyCertificationAudit::getServeProviderId, userId));
            if (ObjectUtil.isNull(agencyCertificationAudit)) {
                throw new ForbiddenOperationException("认证审核信息不存在");
            }
            // 封装返回结果
            CertificationAuditReqDTO certificationAuditReqDTO = new CertificationAuditReqDTO();
            certificationAuditReqDTO.setRejectReason(agencyCertificationAudit.getRejectReason());
            return certificationAuditReqDTO;
        }
        return  null;
    }
    }


