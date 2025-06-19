package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.BankAccountService;
import com.jzo2o.customer.mapper.BankAccountMapper;
import com.jzo2o.mvc.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author 春风
* @description 针对表【bank_account(银行账户)】的数据库操作Service实现
* @createDate 2025-06-19 10:43:41
*/
@Service
public class BankAccountServiceImpl extends ServiceImpl<BankAccountMapper, BankAccount> implements BankAccountService{

    @Autowired
    private BankAccountService bankAccountService;
    /**
     * app新增或修改银行信息
     * @param bankAccountUpsertReqDTO
     */
    @Override
    public void putBankAccount(BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        bankAccountUpsertReqDTO.setId(UserContext.currentUserId());
        BankAccount bankAccount = BeanUtil.copyProperties(bankAccountUpsertReqDTO, BankAccount.class);
        bankAccountService.saveOrUpdate(bankAccount);
    }

    /**
     * 查询当前登录用户的银行信息
     * @return
     */
    @Override
    public BankAccountResDTO currentUserBankAccount() {
        BankAccount bankAccount = bankAccountService.getById(UserContext.currentUserId());
        if (ObjectUtil.isNotNull(bankAccount)) {
            return BeanUtil.copyProperties(bankAccount, BankAccountResDTO.class);
        }
        return null;
    }
}




