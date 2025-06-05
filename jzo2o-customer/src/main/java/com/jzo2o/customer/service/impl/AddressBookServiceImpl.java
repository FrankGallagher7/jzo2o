package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.customer.ServeSkillApi;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.api.publics.MapApi;
import com.jzo2o.api.publics.SmsCodeApi;
import com.jzo2o.api.publics.dto.response.LocationResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.CollUtils;
import com.jzo2o.customer.mapper.AddressBookMapper;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.plaf.synth.Region;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 地址薄 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-06
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {

    @Autowired
    private MapApi mapApi;


    @Override
    public List<AddressBookResDTO> getByUserIdAndCity(Long userId, String city) {

        List<AddressBook> addressBooks = lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getCity, city)
                .list();
        if(CollUtils.isEmpty(addressBooks)) {
            return new ArrayList<>();
        }
        return BeanUtils.copyToList(addressBooks, AddressBookResDTO.class);
    }

    /**
     * 查询用户默认地址值
     * @return
     */
    @Override
    public AddressBookResDTO findDefaultAddress() {

        // 1.获取当前用户id
        Long userId = UserContext.currentUserId();
        // 2.根据用户id查询用户默认地址-is_default = 1
        AddressBook addressBook = baseMapper.selectOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDeleted, 0)
                .eq(AddressBook::getIsDefault, 1));
        if (ObjectUtil.isNull(addressBook)) {
            return null;
        }
        //转换
        return BeanUtil.copyProperties(addressBook,AddressBookResDTO.class);
    }

    /**
     * 新增地址
     * @param addressBookResDTO
     */
    @Override
    public void saveAddress(AddressBookResDTO addressBookResDTO) {
        // 1.获取用户id
        Long userId = UserContext.currentUserId();
        addressBookResDTO.setUserId(userId);
        // 2.判断添加前端经纬度
        if (addressBookResDTO.getLon() == null && addressBookResDTO.getLat() == null) {
            // 添加经纬度信息--远程调用高德服务
            LocationResDTO locationAddress = mapApi.getLocationByAddress(addressBookResDTO.getAddress());
            String location = locationAddress.getLocation();
            // 取出经纬度
            String[] parts = location.split(",");
            double lon = Double.parseDouble(parts[0]); // 经度
            double lat = Double.parseDouble(parts[1]); // 纬度
            // 设置经纬度
            addressBookResDTO.setLon(lon);
            addressBookResDTO.setLat(lat);
        }
        // 3.判读新增地址是否为默认地址，如果是取消旧默认地址
        if (addressBookResDTO.getIsDefault() == 1) {
            List<AddressBook> addressBooks = baseMapper.selectList(new LambdaQueryWrapper<AddressBook>()
                    .eq(AddressBook::getUserId, userId)
                    .eq(AddressBook::getIsDefault, 1)
                    .eq(AddressBook::getIsDeleted, 0));
            // 判断是否有默认地址，有则取消
            if (ObjectUtil.isNotNull(addressBooks) && addressBooks.size() > 0) {
                addressBooks.forEach(addressBook -> {
                    addressBook.setIsDefault(0);
                    baseMapper.updateById(addressBook);
                });
            }
        }
        // 4.保存地址
        AddressBook addressBook = BeanUtil.copyProperties(addressBookResDTO, AddressBook.class);
        baseMapper.insert(addressBook);
    }

    /**
     * 分页查询地址
     * @param pageQueryReqDTO
     * @return
     */
    @Override
    public PageResult<AddressBookResDTO> pageQuery(AddressBookPageQueryReqDTO pageQueryReqDTO) {
        Long userId = UserContext.currentUserId();

        Page<AddressBook> page = PageUtils.parsePageQuery(pageQueryReqDTO, AddressBook.class);
        Page<AddressBook> serveTypePage = baseMapper.selectPage(page, new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId,userId));
        return PageUtils.toPage(serveTypePage, AddressBookResDTO.class);
    }
}
