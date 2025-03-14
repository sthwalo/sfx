from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import func
from datetime import datetime

Base = declarative_base()

class AuditLog(Base):
    __tablename__ = "audit_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    event_type = Column(String(50), nullable=False)  # login, file_upload, file_download, etc.
    user_id = Column(String(50), nullable=True)  # User who performed the action
    timestamp = Column(DateTime, default=func.now(), nullable=False)
    ip_address = Column(String(50), nullable=True)
    details = Column(Text, nullable=True)  # JSON string with additional details
    success = Column(Boolean, default=True)  # Whether the operation succeeded
    
    def __repr__(self):
        return f"AuditLog(id={self.id}, event_type={self.event_type}, user_id={self.user_id}, timestamp={self.timestamp})"
