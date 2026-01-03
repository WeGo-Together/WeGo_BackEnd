package team.wego.wegobackend.chat.domain.entity;

public enum MessageType {
    TEXT,   // 일반 텍스트 메시지
    SYSTEM, // 시스템 메시지 (입장/퇴장 등)
    KICK    // 추방 메시지
}
