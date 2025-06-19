package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;

/**
* @author 春风
* @description 针对表【bank_account(银行账户)】的数据库操作Service
* @createDate 2025-06-19 10:43:41
*/
public interface BankAccountService extends IService<BankAccount> {

    /**
     * app新增或修改银行信息
     * @param bankAccountUpsertReqDTO
     */
    void putBankAccount(BankAccountUpsertReqDTO bankAccountUpsertReqDTO);
}
