package com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Entity
@Table(name = "comment")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long commentId;

  @Column(name = "worklog_id", nullable = false)
  private Long worklogId;

  @Column(name = "comment_content", nullable = false)
  private String commentContent;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Builder
  public Comment(Long worklogId, String commentContent, Long userId) {
    this.worklogId = worklogId;
    this.commentContent = commentContent;
    this.userId = userId;
  }

  /** 💡 여기가 핵심: 엔티티 저장 직전에 createdAt 수동 설정 */
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public void updateContent(String content) {
    this.commentContent = content;
    this.updatedAt = LocalDateTime.now();
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }
}
