package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskRepositoryTest {

    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
    }

    @Test
    void save_ShouldAssignIdAndPersistTask() {
        Task task = Task.builder()
                .title("Domain Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .studentId("S1")
                .subjectId("MATH-101")
                .build();

        Task result = repository.save(task);

        assertNotNull(result.getId());
        assertEquals(TaskStatus.TODO, result.getStatus());
    }

    @Test
    void save_WhenIdAlreadySet_ShouldPreserveId() {
        Task task = Task.builder()
                .id("existing-id")
                .title("Task")
                .studentId("S1")
                .subjectId("MATH-101")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .build();

        Task result = repository.save(task);

        assertEquals("existing-id", result.getId());
    }

    @Test
    void findByStudentId_ShouldReturnOnlyMatchingTasks() {
        repository.save(task("S1", "MATH", "T1"));
        repository.save(task("S1", "PHYS", "T2"));
        repository.save(task("S2", "MATH", "T3"));

        List<Task> result = repository.findByStudentId("S1");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> "S1".equals(t.getStudentId())));
    }

    @Test
    void existsDuplicate_ShouldDetectCaseInsensitiveDuplicates() {
        repository.save(task("S1", "MATH", "My Task"));

        assertTrue(repository.existsDuplicate("S1", "MATH", "my task"));
        assertFalse(repository.existsDuplicate("S1", "MATH", "Other Task"));
        assertFalse(repository.existsDuplicate("S2", "MATH", "My Task"));
    }

    @Test
    void findById_ShouldReturnPresentWhenExists() {
        Task saved = repository.save(task("S1", "MATH", "T1"));

        Optional<Task> result = repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotFound() {
        Optional<Task> result = repository.findById("non-existent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByStudentIdWithFilters_ShouldFilterByStatus() {
        Task todo = task("S1", "MATH", "T1");
        todo.setStatus(TaskStatus.TODO);
        todo.setDeadline(LocalDateTime.now().plusDays(3));

        Task done = task("S1", "PHYS", "T2");
        done.setStatus(TaskStatus.COMPLETED);
        done.setDeadline(LocalDateTime.now().plusDays(3));

        repository.save(todo);
        repository.save(done);

        List<Task> result = repository.findByStudentIdWithFilters("S1", TaskStatus.TODO, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatus());
    }

    @Test
    void findByStudentIdWithFilters_ShouldFilterByDateRange() {
        Task early = task("S1", "MATH", "T1");
        early.setDeadline(LocalDateTime.now().plusDays(1));
        early.setStatus(TaskStatus.TODO);

        Task late = task("S1", "PHYS", "T2");
        late.setDeadline(LocalDateTime.now().plusDays(10));
        late.setStatus(TaskStatus.TODO);

        repository.save(early);
        repository.save(late);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(5);

        List<Task> result = repository.findByStudentIdWithFilters("S1", null, start, end, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void saveAll_ShouldPersistAllTasksAndAssignIds() {
        List<Task> tasks = List.of(
                task("S1", "MATH", "T-A"),
                task("S1", "PHYS", "T-B"),
                task("S2", "CHEM", "T-C")
        );

        List<Task> saved = repository.saveAll(tasks);

        assertEquals(3, saved.size());
        saved.forEach(t -> assertNotNull(t.getId()));
    }

    @Test
    void findByStudentIdWithFilters_WithNoFilters_ShouldReturnAllStudentTasks() {
        repository.save(task("S9", "MATH", "X1"));
        repository.save(task("S9", "PHYS", "X2"));

        List<Task> result = repository.findByStudentIdWithFilters("S9", null, null, null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void save_WhenIdIsBlank_ShouldGenerateNewId() {
        Task task = Task.builder()
                .id("   ")
                .title("Task with blank id")
                .studentId("S1")
                .subjectId("MATH-101")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Task result = repository.save(task);

        assertNotNull(result.getId());
        assertFalse(result.getId().isBlank());
    }

    @Test
    void existsDuplicate_WhenSubjectIdDiffers_ShouldReturnFalse() {
        repository.save(task("S1", "MATH", "My Task"));

        assertFalse(repository.existsDuplicate("S1", "PHYS", "My Task"));
    }

    @Test
    void findByStudentIdWithFilters_WhenTaskHasNullDeadlineAndStartDateProvided_ShouldExcludeTask() {
        Task taskNoDeadline = Task.builder()
                .studentId("S1").subjectId("MATH").title("No Deadline")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .deadline(null)
                .estimatedDurationMinutes(60)
                .build();
        repository.save(taskNoDeadline);

        LocalDateTime start = LocalDateTime.now();
        List<Task> result = repository.findByStudentIdWithFilters("S1", null, start, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByStudentIdWithFilters_WhenTaskHasNullDeadlineAndEndDateProvided_ShouldExcludeTask() {
        Task taskNoDeadline = Task.builder()
                .studentId("S1").subjectId("MATH").title("No Deadline End")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .deadline(null)
                .estimatedDurationMinutes(60)
                .build();
        repository.save(taskNoDeadline);

        LocalDateTime end = LocalDateTime.now().plusDays(5);
        List<Task> result = repository.findByStudentIdWithFilters("S1", null, null, end, null, null);

        assertTrue(result.isEmpty());
    }

    private Task task(String studentId, String subjectId, String title) {
        return Task.builder()
                .studentId(studentId)
                .subjectId(subjectId)
                .title(title)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .deadline(LocalDateTime.now().plusDays(5))
                .estimatedDurationMinutes(60)
                .build();
    }

    // R41 — subjectId / taskType filter tests
    @Test
    void findByStudentIdWithFilters_WhenSubjectIdFilter_ShouldReturnMatchingTasks() {
        Task mathTask = task("S1", "MATH-101", "Tarea de mates");
        Task physTask = task("S1", "PHYS-201", "Tarea de física");
        repository.save(mathTask);
        repository.save(physTask);

        List<Task> result = repository.findByStudentIdWithFilters("S1", null, null, null, "MATH-101", null);

        assertEquals(1, result.size());
        assertEquals("MATH-101", result.get(0).getSubjectId());
    }

    @Test
    void findByStudentIdWithFilters_WhenTaskTypeFilter_ShouldReturnMatchingTasks() {
        Task exam = Task.builder()
                .studentId("S1").subjectId("MATH-101").title("Examen parcial")
                .status(TaskStatus.TODO).priority(TaskPriority.HIGH)
                .taskType(TaskType.EXAMEN).deadline(LocalDateTime.now().plusDays(5))
                .estimatedDurationMinutes(90).build();

        Task homework = Task.builder()
                .studentId("S1").subjectId("MATH-101").title("Tarea de álgebra")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .taskType(TaskType.TAREA).deadline(LocalDateTime.now().plusDays(5))
                .estimatedDurationMinutes(60).build();

        repository.save(exam);
        repository.save(homework);

        List<Task> result = repository.findByStudentIdWithFilters("S1", null, null, null, null, TaskType.EXAMEN);

        assertEquals(1, result.size());
        assertEquals(TaskType.EXAMEN, result.get(0).getTaskType());
    }

    @Test
    void deleteById_ShouldRemoveTaskFromRepository() {
        Task saved = repository.save(task("S1", "MATH", "To Delete"));
        String id = saved.getId();

        assertTrue(repository.findById(id).isPresent());

        repository.deleteById(id);

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void deleteById_WhenIdNotFound_ShouldNotThrow() {
        assertDoesNotThrow(() -> repository.deleteById("non-existent-id"));
    }
}
