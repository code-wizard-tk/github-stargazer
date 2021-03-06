package com.thomaskioko.githubstargazer.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.thomaskioko.githubstargazer.mock.MockData.makeRepoEntity
import com.thomaskioko.githubstargazer.mock.MockData.makeRepoEntityList
import com.thomaskioko.githubstargazer.mock.MockData.makeRepoResponseList
import com.thomaskioko.githubstargazer.mock.MockData.makeTrendingRepoEntityList
import com.thomaskioko.githubstargazer.mock.MockData.makeTrendingRepoResponseList
import com.thomaskioko.stargazer.api.service.GitHubService
import com.thomaskioko.stargazer.db.GithubDatabase
import com.thomaskioko.stargazer.db.dao.RepoDao
import com.thomaskioko.stargazer.repository.GithubRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class GithubRepositoryTest {

    private val repoDao: RepoDao = mock {
        onBlocking { getRepositories() } doReturn makeRepoEntityList()
        onBlocking { getTrendingRepositories() } doReturn makeTrendingRepoEntityList()
        onBlocking { getBookmarkedRepos() } doReturn makeRepoEntityList()
        onBlocking { getRepoById(1234) } doReturn makeRepoEntity(1234, false)
    }

    private val database: GithubDatabase = mock {
        on { repoDao() } doReturn repoDao
    }
    private val service: GitHubService = mock {
        onBlocking { getRepositories() } doReturn makeRepoResponseList()
        onBlocking { getTrendingRepositories() } doReturn makeTrendingRepoResponseList()
    }

    private val testDispatcher = TestCoroutineDispatcher()

    private var repository = GithubRepository(service, database, testDispatcher)

    @Test
    fun `givenDeviceIsConnected andCacheHasNoData verify data isLoadedFrom Remote`() {
        runBlocking {

            repository.getRepositoryList(true).test {
                assertEquals(expectItem(), makeRepoEntityList())
                expectComplete()
            }

            verify(service).getRepositories()
            verify(database.repoDao()).insertRepos(makeRepoEntityList())
            verify(database.repoDao()).getRepositories()
        }
    }

    @Test
    fun givenDeviceIsConnected_andCacheHasNoData_getTrendingRepositories_isLoadedFromRemote() {
        runBlocking {

            repository.getTrendingTrendingRepositories(true).test {
                assertEquals(expectItem(), makeTrendingRepoEntityList())
                expectComplete()
            }

            verify(service).getTrendingRepositories()
            verify(database.repoDao()).insertRepos(makeTrendingRepoEntityList())
            verify(database.repoDao()).getTrendingRepositories()
        }
    }

    @Test
    fun givenDeviceIsNotConnected_verifyTrendingDataIsLoadedFromDatabase() {
        runBlocking {

            repository.getTrendingTrendingRepositories(false).test {
                assertEquals(expectItem(), makeTrendingRepoEntityList())
                expectComplete()
            }

            verify(service, never()).getTrendingRepositories()
            verify(database.repoDao()).getTrendingRepositories()
        }
    }

    @Test
    fun givenDeviceIsNotConnected_verify_dataIsLoadedFromDatabase() {
        runBlocking {

            whenever(repoDao.getRepositories()).thenReturn(makeRepoEntityList())

            repository.getRepositoryList(false).test {
                assertEquals(expectItem(), makeRepoEntityList())
                expectComplete()
            }

            verify(service, never()).getRepositories()
            verify(database.repoDao()).getRepositories()
        }
    }

    @Test
    fun `givenRepoId verify data isLoadedFrom Database`() = runBlocking {
        val expected = makeRepoEntity(1234, false)

        val repoEntity = repository.getRepoById(expected.repoId)

        verify(database.repoDao()).getRepoById(expected.repoId)

        assertThat(repoEntity).isEqualTo(expected)
    }

    @Test
    fun givenGetBookmarkedRepos_isInvoked_verify_EntityListIsReturned() = runBlocking {

        val repoEntity = repository.getBookmarkedRepos()

        verify(database.repoDao()).getBookmarkedRepos()

        assertThat(repoEntity).isEqualTo(makeRepoEntityList())
    }

    @Test
    fun givenRepoIsUpdated_verify_correctValuesArePassedToDb() = runBlocking {

        repository.updateRepoBookMarkStatus(1, 3)

        verify(database.repoDao()).setBookmarkStatus(1, 3)
    }
}
