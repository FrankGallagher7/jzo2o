package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.mapper.WorkerCertificationMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationResDTO;
import com.jzo2o.customer.service.IWorkerCertificationService;
import com.jzo2o.mvc.utils.UserContext;
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
public class WorkerCertificationServiceImpl extends ServiceImpl<WorkerCertificationMapper, WorkerCertification> implements IWorkerCertificationService {

    @Autowired
    private WorkerCertificationAuditMapper workerCertificationAuditMapper;



    /**
     * 根据服务人员id更新
     *
     * @param workerCertificationUpdateDTO 服务人员认证更新模型
     */
    @Override
    public void updateById(WorkerCertificationUpdateDTO workerCertificationUpdateDTO) {
        LambdaUpdateWrapper<WorkerCertification> updateWrapper = Wrappers.<WorkerCertification>lambdaUpdate()
                .eq(WorkerCertification::getId, workerCertificationUpdateDTO.getId())
                .set(WorkerCertification::getCertificationStatus, workerCertificationUpdateDTO.getCertificationStatus())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getName()), WorkerCertification::getName, workerCertificationUpdateDTO.getName())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getIdCardNo()), WorkerCertification::getIdCardNo, workerCertificationUpdateDTO.getIdCardNo())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getFrontImg()), WorkerCertification::getFrontImg, workerCertificationUpdateDTO.getFrontImg())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getBackImg()), WorkerCertification::getBackImg, workerCertificationUpdateDTO.getBackImg())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getCertificationMaterial()), WorkerCertification::getCertificationMaterial, workerCertificationUpdateDTO.getCertificationMaterial())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getCertificationTime()), WorkerCertification::getCertificationTime, workerCertificationUpdateDTO.getCertificationTime());
        super.update(updateWrapper);
    }

    /**
     * app新增认证申请
     * @param workerCertificationAuditAddReqDTO
     */
    @Override
    public void postCertification(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        // 1.新增认证信息
        Long userId = UserContext.currentUserId();
        WorkerCertification workerCertification = BeanUtil.copyProperties(workerCertificationAuditAddReqDTO, WorkerCertification.class);
        workerCertification.setId(userId); // 设置用户id
        workerCertification.setCertificationTime(LocalDateTime.now()); // 设置认证时间
        workerCertification.setCertificationStatus(1); // 设置认证状态
        baseMapper.insert(workerCertification);

        // 2.新增认证审核信息
        WorkerCertificationAudit workerCertificationAudit = new WorkerCertificationAudit();
        workerCertificationAudit.setServeProviderId(workerCertification.getId());
        workerCertificationAudit.setName(workerCertification.getName());
        workerCertificationAudit.setIdCardNo(workerCertification.getIdCardNo());
        workerCertificationAudit.setFrontImg(workerCertification.getFrontImg());
        workerCertificationAudit.setBackImg(workerCertification.getBackImg());
        workerCertificationAudit.setCertificationMaterial(workerCertification.getCertificationMaterial());
        workerCertificationAudit.setAuditStatus(0);
        workerCertificationAudit.setCertificationStatus(1);

        workerCertificationAuditMapper.insert(workerCertificationAudit);
    }

    /**
     * 获取拒绝原因
     * @return
     */
    @Override
    public CertificationAuditReqDTO getRejectReason() {
        // 1.当前认证状态
        Long userId = UserContext.currentUserId();
        WorkerCertification workerCertification = baseMapper.selectById(userId);
        if (ObjectUtil.isNull(workerCertification)) {
            throw new ForbiddenOperationException("认证信息不存在");
        }
        // 2.认证状态
        if (workerCertification.getCertificationStatus() == 3) {
            WorkerCertificationAudit workerCertificationAudit = workerCertificationAuditMapper.selectOne(new LambdaQueryWrapper<WorkerCertificationAudit>().eq(WorkerCertificationAudit::getServeProviderId, userId));
            if (ObjectUtil.isNull(workerCertificationAudit)) {
                throw new ForbiddenOperationException("认证信息不存在");
            }
            // 封装返回结果
            CertificationAuditReqDTO certificationAuditReqDTO = new CertificationAuditReqDTO();
            certificationAuditReqDTO.setRejectReason(workerCertificationAudit.getRejectReason());
            return certificationAuditReqDTO;
        }
        return  null;
    }
}
