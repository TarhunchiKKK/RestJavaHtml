package com.example.rab56.database;

import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RepositoryServiceTest {

    private Repository repository = new Repository(){

        @Override
        public List<DbEntity> findAll(Sort sort) {
            return null;
        }

        @Override
        public Page<DbEntity> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends DbEntity> S save(S entity) {
            return null;
        }

        @Override
        public <S extends DbEntity> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<DbEntity> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public List<DbEntity> findAll() {
            return null;
        }

        @Override
        public List<DbEntity> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Integer integer) {

        }

        @Override
        public void delete(DbEntity entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends DbEntity> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public void flush() {

        }

        @Override
        public <S extends DbEntity> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends DbEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
            return null;
        }

        @Override
        public void deleteAllInBatch(Iterable<DbEntity> entities) {

        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Integer> integers) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public DbEntity getOne(Integer integer) {
            return null;
        }

        @Override
        public DbEntity getById(Integer integer) {
            return null;
        }

        @Override
        public DbEntity getReferenceById(Integer integer) {
            return null;
        }

        @Override
        public <S extends DbEntity> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends DbEntity> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends DbEntity> List<S> findAll(Example<S> example, Sort sort) {
            return null;
        }

        @Override
        public <S extends DbEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends DbEntity> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends DbEntity> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends DbEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
    };
    private RepositoryService service = new RepositoryService(repository);
    @Test
    public void testAll(){

        Assertions.assertNull(service.getAll());
        service.save(new Numbers(new int[] {1,2,3,4}), new ResultPair(2.0,2.0));
        service.save(new Numbers(new int[] {5,6,7,8}), new ResultPair(6.0,6.0));
        Assertions.assertEquals(service.getAll().size(), 2);
    }
}
