package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.mapper.WorkerCertificationMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationService;
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
public class WorkerCertificationAuditServiceImpl extends ServiceImpl<WorkerCertificationAuditMapper, WorkerCertificationAudit> implements IWorkerCertificationAuditService {

    @Autowired
    private WorkerCertificationMapper workerCertificationMapper;

    /**
     * 审核服务人员认证分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult<WorkerCertificationAudit> pageQuery(WorkerCertificationAuditPageQueryReqDTO dto) {
        Page<WorkerCertificationAudit> page = PageUtils.parsePageQuery(dto, WorkerCertificationAudit.class);


        Page<WorkerCertificationAudit> certificationAuditPage = baseMapper.selectPage(page,
                new LambdaQueryWrapper<WorkerCertificationAudit>()
                        .eq(StringUtil.isNotEmpty(dto.getIdCardNo()), WorkerCertificationAudit::getIdCardNo, dto.getIdCardNo())
                        .like(StringUtil.isNotEmpty(dto.getName()), WorkerCertificationAudit::getName, dto.getName())
                        .eq(dto.getAuditStatus() != null, WorkerCertificationAudit::getAuditStatus, dto.getAuditStatus())
                        .eq(dto.getCertificationStatus() != null, WorkerCertificationAudit::getCertificationStatus, dto.getCertificationStatus()));

        return PageUtils.toPage(certificationAuditPage,WorkerCertificationAudit.class);
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
        WorkerCertificationAudit workerCertificationAudit = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(workerCertificationAudit)) {
            throw new ForbiddenOperationException("当前认证信息不存在");
        }
        WorkerCertification workerCertification = workerCertificationMapper.selectById(workerCertificationAudit.getServeProviderId());

        // 2.获取设置认证状态
        Integer certificationStatus = dto.getCertificationStatus();
        if ( dto.getCertificationStatus() == 2) { //认证成功
            workerCertificationAudit.setAuditStatus(1); //审核状态-0：未审核，1：已审核',
            workerCertificationAudit.setAuditorId(userId); //审核人id
            workerCertificationAudit.setAuditorName(name); //审核人名称
            workerCertificationAudit.setAuditTime(LocalDateTime.now()); //审核时间
            workerCertificationAudit.setCertificationStatus(2); //1：认证中，2：认证成功，3认证失败'
            // 修改审核表状态
            baseMapper.updateById(workerCertificationAudit);
            // 修改认证表状态
            workerCertification.setCertificationStatus(2);
            workerCertificationMapper.updateById(workerCertification);
        }
        // 认证失败
        workerCertificationAudit.setAuditStatus(1); //审核状态-0：未审核，1：已审核',
        workerCertificationAudit.setAuditorId(userId); //审核人id
        workerCertificationAudit.setAuditorName(name); //审核人名称
        workerCertificationAudit.setAuditTime(LocalDateTime.now()); //审核时间
        workerCertificationAudit.setCertificationStatus(certificationStatus); //1：认证中，2：认证成功，3认证失败'
        workerCertificationAudit.setRejectReason(dto.getRejectReason()); //认证失败原因
        // 修改审核表状态
        baseMapper.updateById(workerCertificationAudit);
        // 修改认证表状态
        workerCertification.setCertificationStatus(3);
    }
}
