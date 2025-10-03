    package com.example.expensetracker.logging.applog;
    
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    
    @Service
    public class AppLogService {
    
        private final AppLogRepository appLogRepository;
    
        public AppLogService(AppLogRepository appLogRepository) {
            this.appLogRepository = appLogRepository;
        }
    
        public AppLogDto log(AppLogDto dto) {
            AppLog entity = AppLog.from(dto);
            AppLog saved = appLogRepository.save(entity);
            return AppLogDto.from(saved);
        }
    
        public Page<AppLogDto> findAll(Pageable pageable) {
            return appLogRepository.findAll(pageable).map(AppLogDto::from);
        }
    
        public Page<AppLogDto> findByUserEmail(String email, Pageable pageable) {
            return appLogRepository.findByUserEmail(email, pageable).map(AppLogDto::from);
        }
    }
