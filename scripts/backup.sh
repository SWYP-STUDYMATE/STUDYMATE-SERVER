#!/bin/bash

# STUDYMATE Database Backup Script
# This script creates daily backups of the MySQL database

set -e  # Exit on any error

# Configuration
MYSQL_HOST="${MYSQL_HOST:-db}"
MYSQL_USER="${MYSQL_USER:-studymate}"
MYSQL_PASSWORD="${MYSQL_PASSWORD}"
MYSQL_DATABASE="${MYSQL_DATABASE:-studymate}"
BACKUP_DIR="/backups"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-7}"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Generate backup filename with timestamp
BACKUP_DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/studymate_backup_$BACKUP_DATE.sql"

echo "Starting database backup at $(date)"
echo "Backup file: $BACKUP_FILE"

# Create backup
if mysqldump -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" \
    --routines --triggers --single-transaction --lock-tables=false \
    --add-drop-database --databases "$MYSQL_DATABASE" > "$BACKUP_FILE"; then
    
    echo "Database backup completed successfully"
    
    # Compress the backup file
    gzip "$BACKUP_FILE"
    echo "Backup file compressed: ${BACKUP_FILE}.gz"
    
    # Remove old backup files (older than RETENTION_DAYS)
    find "$BACKUP_DIR" -name "studymate_backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete
    echo "Old backup files cleaned up (retention: $RETENTION_DAYS days)"
    
    # Display backup file size
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}.gz" | cut -f1)
    echo "Backup size: $BACKUP_SIZE"
    
    echo "Backup process completed successfully at $(date)"
else
    echo "ERROR: Database backup failed at $(date)" >&2
    exit 1
fi

# List current backup files
echo "Current backup files:"
ls -lah "$BACKUP_DIR"/studymate_backup_*.sql.gz 2>/dev/null || echo "No backup files found"