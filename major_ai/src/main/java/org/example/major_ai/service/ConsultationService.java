package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ConsultationPostEntity;
import org.example.major_ai.entity.DoctorReplyEntity;
import org.example.major_ai.mapper.ConsultationPostMapper;
import org.example.major_ai.mapper.DoctorReplyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 社区问诊服务
 * 处理用户发布问诊帖和医生回复的业务逻辑
 *
 * 核心功能：
 * - 用户发布问诊帖（createPost）
 * - 查询帖子列表（listPosts）
 * - 医生回复帖子（addReply）
 * - 关闭帖子（closePost）
 *
 * 帖子状态流转：
 * PENDING（待回复）→ REPLIED（已回复）→ CLOSED（已关闭）
 *
 * 注意：这是社区问诊（人工），不是AI问诊
 */
@Service
@RequiredArgsConstructor
public class ConsultationService {

    /** 帖子Mapper，负责consultation_post表的CRUD */
    private final ConsultationPostMapper consultationPostMapper;

    /** 回复Mapper，负责doctor_reply表的CRUD */
    private final DoctorReplyMapper doctorReplyMapper;

    /**
     * 发布问诊帖
     * 用户提交问诊信息后，创建一个新的帖子
     *
     * @param post 帖子实体（包含用户ID、科室ID、标题、内容等）
     * @return 创建成功的帖子（包含生成的ID和时间戳）
     */
    public ConsultationPostEntity createPost(ConsultationPostEntity post) {
        // 获取当前时间戳（毫秒）
        long now = System.currentTimeMillis();

        // 设置创建时间和更新时间
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        // 设置初始状态为"待回复"
        // 状态流转：PENDING → REPLIED → CLOSED
        post.setStatus("PENDING");

        // 插入数据库
        // SQL: INSERT INTO consultation_post(user_id, department_id, title, content, status, created_at, updated_at)
        //      VALUES(?, ?, ?, ?, 'PENDING', ?, ?)
        consultationPostMapper.insert(post);

        return post;
    }

    /**
     * 查询帖子列表（支持按科室和状态筛选）
     *
     * @param departmentId 科室ID（可选，为null则不按科室筛选）
     * @param status 帖子状态（可选，为null则不按状态筛选）
     * @return 符合条件的帖子列表，按创建时间降序排列
     */
    public List<ConsultationPostEntity> listPosts(Long departmentId, String status) {
        // 创建查询条件构建器
        LambdaQueryWrapper<ConsultationPostEntity> wrapper = new LambdaQueryWrapper<>();

        // 按科室筛选（如果传入了departmentId）
        if (departmentId != null) {
            wrapper.eq(ConsultationPostEntity::getDepartmentId, departmentId);
        }

        // 按状态筛选（如果传入了status）
        // 常用状态：PENDING（待回复）、REPLIED（已回复）、CLOSED（已关闭）
        if (status != null) {
            wrapper.eq(ConsultationPostEntity::getStatus, status);
        }

        // 按创建时间降序排列（最新的帖子在前面）
        wrapper.orderByDesc(ConsultationPostEntity::getCreatedAt);

        // 执行查询
        // SQL: SELECT * FROM consultation_post
        //      WHERE department_id = ? AND status = ?
        //      ORDER BY created_at DESC
        return consultationPostMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询帖子详情
     *
     * @param id 帖子ID
     * @return 帖子实体，不存在则返回null
     */
    public ConsultationPostEntity findPostById(Long id) {
        // SQL: SELECT * FROM consultation_post WHERE id = ?
        return consultationPostMapper.selectById(id);
    }

    /**
     * 查询指定用户的帖子列表
     *
     * @param userId 用户ID
     * @return 该用户的所有帖子，按创建时间降序排列
     */
    public List<ConsultationPostEntity> listPostsByUserId(Long userId) {
        // 构建查询条件：user_id = userId，按创建时间降序
        return consultationPostMapper.selectList(
                new LambdaQueryWrapper<ConsultationPostEntity>()
                        .eq(ConsultationPostEntity::getUserId, userId)
                        .orderByDesc(ConsultationPostEntity::getCreatedAt)
        );
        // SQL: SELECT * FROM consultation_post
        //      WHERE user_id = ?
        //      ORDER BY created_at DESC
    }

    /**
     * 医生回复问诊帖
     * 1. 保存回复内容
     * 2. 更新帖子状态为"已回复"
     *
     * @param reply 回复实体（包含帖子ID、医生ID、回复内容等）
     * @return 创建成功的回复（包含生成的ID和时间戳）
     */
    public DoctorReplyEntity addReply(DoctorReplyEntity reply) {
        // 设置回复的创建时间
        reply.setCreatedAt(System.currentTimeMillis());

        // 插入回复到数据库
        // SQL: INSERT INTO doctor_reply(post_id, doctor_id, content, created_at)
        //      VALUES(?, ?, ?, ?)
        doctorReplyMapper.insert(reply);

        // 更新帖子状态为"已回复"
        // 注意：这里只更新状态和时间，不更新其他字段
        ConsultationPostEntity post = new ConsultationPostEntity();
        post.setId(reply.getPostId());           // 帖子ID
        post.setStatus("REPLIED");               // 状态：已回复
        post.setUpdatedAt(System.currentTimeMillis());  // 更新时间

        // SQL: UPDATE consultation_post
        //      SET status = 'REPLIED', updated_at = ?
        //      WHERE id = ?
        consultationPostMapper.updateById(post);

        return reply;
    }

    /**
     * 查询指定帖子的所有回复
     *
     * @param postId 帖子ID
     * @return 该帖子的所有回复列表，按创建时间升序排列（先回复的在前面）
     */
    public List<DoctorReplyEntity> listReplies(Long postId) {
        // 构建查询条件：post_id = postId，按创建时间升序
        return doctorReplyMapper.selectList(
                new LambdaQueryWrapper<DoctorReplyEntity>()
                        .eq(DoctorReplyEntity::getPostId, postId)
                        .orderByAsc(DoctorReplyEntity::getCreatedAt)
        );
        // SQL: SELECT * FROM doctor_reply
        //      WHERE post_id = ?
        //      ORDER BY created_at ASC
    }

    /**
     * 关闭问诊帖
     * 用户可以关闭自己的帖子，关闭后不再接受新回复
     *
     * @param postId 帖子ID
     * @return 关闭成功返回true，帖子不存在返回false
     */
    public boolean closePost(Long postId) {
        // 创建帖子实体，只设置需要更新的字段
        ConsultationPostEntity post = new ConsultationPostEntity();
        post.setId(postId);                      // 帖子ID
        post.setStatus("CLOSED");                // 状态：已关闭
        post.setUpdatedAt(System.currentTimeMillis());  // 更新时间

        // 更新帖子状态
        // SQL: UPDATE consultation_post
        //      SET status = 'CLOSED', updated_at = ?
        //      WHERE id = ?
        // updateById返回影响的行数，>0表示更新成功
        return consultationPostMapper.updateById(post) > 0;
    }
}
