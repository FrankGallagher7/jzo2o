package com.jzo2o.customer.service;

import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 地址薄 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-06
 */
public interface IAddressBookService extends IService<AddressBook> {

    /**
     * 根据用户id和城市编码获取地址
     *
     * @param userId 用户id
     * @param cityCode 城市编码
     * @return 地址编码
     */
    List<AddressBookResDTO> getByUserIdAndCity(Long userId, String cityCode);

    /**
     * 查询用户默认地址值
     * @return
     */
    AddressBookResDTO findDefaultAddress();

    /**
     * 新增地址
     * @param addressBookResDTO
     */
    void saveAddress(AddressBookResDTO addressBookResDTO);

    /**
     * 分页查询地址
     * @param pageQueryReqDTO
     * @return
     */
    PageResult<AddressBookResDTO> pageQuery(AddressBookPageQueryReqDTO pageQueryReqDTO);

    /**
     * 根据地址id对地址进行编辑
     * @param id
     * @param addressBookResDTO
     */
    void updateAddress(Long id, AddressBookResDTO addressBookResDTO);

    /**
     * 批量删除地址
     * @param ids
     */
    void removeUpdateByIds(List<Long> ids);

    /**
     * 设置默认地址
     * @param id
     * @param flag
     */
    void setDefaultAddress(Long id, Long flag);
}
