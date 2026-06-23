package com.taotie.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.taotie.constant.MessageConstant;
import com.taotie.constant.StatusConstant;
import com.taotie.dto.DishDTO;
import com.taotie.dto.DishPageQueryDTO;
import com.taotie.entity.Dish;
import com.taotie.entity.DishFlavor;
import com.taotie.entity.Setmeal;
import com.taotie.exception.DeletionNotAllowedException;
import com.taotie.mapper.DishFlavorMapper;
import com.taotie.mapper.DishMapper;
import com.taotie.mapper.SetmealDishMapper;
import com.taotie.mapper.SetmealMapper;
import com.taotie.result.PageResult;
import com.taotie.service.DishService;
import com.taotie.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO){

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入1条数据
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.insert(dish);

        //获取insert语句获取的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size() > 0){
            flavors.forEach(disFlavor -> {
                disFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

        stringRedisTemplate.delete("dish_" + dishDTO.getCategoryId());
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //select * from employee limit 0,10
        //开始分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除---是否存在起售中的菜品？？
        List<Long> categoryIds = new ArrayList<>();
        for(Long id:ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //当前菜品处于起售状态，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            categoryIds.add(dish.getCategoryId());
        }
        //判断当前菜品是否能够删除---是否被套餐关联了？？
        List<Long> SetmealIds = setmealDishMapper.getSetmealIdsByDishId (ids);
        if(SetmealIds != null && SetmealIds.size() > 0){
            //当前菜品被套餐关联不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的菜品
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除菜品关联口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //根据菜品id集合批量删除菜品数据
        dishMapper.deleteByIds(ids);
        //根据菜品id集合批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

        categoryIds.forEach(cid -> stringRedisTemplate.delete("dish_" + cid));
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id){

        //根据id查询菜品查询
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    public Dish getById(Long id) {
        return dishMapper.getById(id);
    }

    public Integer countByStatus(Integer status) {
        Map map = new HashMap();
        map.put("status", status);
        return dishMapper.countByMap(map);
    }

    /**
     * 修改菜品及其关联的口味
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //修改菜品表基本信息
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size() > 0){
            flavors.forEach(disFlavor -> {
                disFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

        stringRedisTemplate.delete("dish_" + dishDTO.getCategoryId());
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status,Long id){
        Dish oldDish = dishMapper.getById(id);

        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        stringRedisTemplate.delete("dish_" + oldDish.getCategoryId());

        if(status == StatusConstant.DISABLE){
            //如果是停售操作，那么对应的套餐也要停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId (dishIds);
            if(setmealIds != null && setmealIds.size() > 0){
                for(Long setmealId : setmealIds){
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categroyId
     * @return
     */
    public List<Dish> list(Long categroyId){
        Dish dish = Dish.builder()
                .categoryId(categroyId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        String key = "dish_" + dish.getCategoryId();

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSON.parseArray(cached, DishVO.class);
        }

        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(dishVOList), 1, TimeUnit.HOURS);
        return dishVOList;
    }

}
