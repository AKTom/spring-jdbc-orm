package com.egzosn.infrastructure.database.jdbc.id;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 储存id的一些信息
 * @author egan
 * @email egzosn@gmail.com
 * @date 2017/11/27
 */
public class IdField {

    /**
     * id字段对应的可写方法
     */
    private Method writeMethod;
    /**
     * id字段对应的可读方法
     */
    private Method readMethod;
    /**
     * sql列名字
     */
    private String column;
    /**
     * id 的属性名字
     */
    private String name;
    /**
     * 主键字段类型
     */
    private Class type;

    /**
     * 主键生成策略
     */
    private IdGeneratedStrategy strategy;


    /**
     * 对应实体id字段进行设值
     * @param entity 实体
     * @param ps 数据库操作对象
     */
    public <T>Serializable idGenerated(T entity, PreparedStatement ps){
            try {
                Object result = getReadMethod().invoke(entity);
                if (null != result){
                    throw new IdentifierGenerationException(String.format("id生成失败，该对象{%s}的id已有值:", entity, result));
                }
                KeyHolder keyHolder = getStrategy().generation(ps, 1);
                Serializable key = keyHolder.key();
                if (null != key) {
                    getWriteMethod().invoke(entity, key);
                }
                return key;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        return null;
    }
    /**
     * 对应实体id字段进行设值
     * @param entitys 实体
     * @param ps 数据库操作对象
     */
    public <T>List<Serializable> idGenerated(Collection<T> entitys, PreparedStatement ps){
        List<Serializable> ids = new ArrayList<>();
            try {
                List<Map<String, Object>> keyList = null;
                int i = 0;
                for (T entity : entitys) {
                    Object result = getReadMethod().invoke(entity);
                    if (null != result) {
                        throw new IdentifierGenerationException(String.format("id生成失败，该对象{%s}的id已有值:", entitys, result));
                    }
                    if (null == keyList ){
                        KeyHolder keyHolder = getStrategy().generation(ps, entitys.size());
                        if (null == keyHolder){
                            return null;
                        }
                         keyList = keyHolder.getKeyList();
                    }
                    for (Object id :  keyList.get(i++).values()){
                        if (null != id) {
                            getWriteMethod().invoke(entitys, new Object[]{id});
                            ids.add((Serializable) id);
                        }
                        break;
                    }

                }
                return ids;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        return ids;
    }

    /**
     * 是否有id生成策略
     * @return  是否有id生成策略
     */
    public boolean isIdGeneratedStrategy(){
        return null != this.getStrategy();
    }


    public Method getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public IdGeneratedStrategy getStrategy() {
        return strategy;
    }

    /**
     *
     *   a flag indicating whether auto-generated keys
     *        should be returned; one of
     *        true <code>Statement.RETURN_GENERATED_KEYS</code> or
     *        false <code>Statement.NO_GENERATED_KEYS</code>
     */
    public boolean autoGeneratedKeys() {
        return GenerationType.AUTO == strategy;
    }

    public void setStrategy(IdGeneratedStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "IdField{" +
                "writeMethod=" + writeMethod +
                ", readMethod=" + readMethod +
                ", column='" + column + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", strategy=" + strategy +
                '}';
    }
}
