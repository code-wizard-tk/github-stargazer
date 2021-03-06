package com.thomaskioko.stargazer.bookmarks.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.thomaskioko.stargazer.bookmarks.R
import com.thomaskioko.stargazer.bookmarks.databinding.FragmentBookmarkedReposBinding
import com.thomaskioko.stargazer.bookmarks.model.RepoViewDataModel
import com.thomaskioko.stargazer.bookmarks.ui.adapter.BookmarkRepoItemClick
import com.thomaskioko.stargazer.bookmarks.ui.adapter.BookmarkedReposAdapter
import com.thomaskioko.stargazer.bookmarks.ui.viewmodel.GetBookmarkedReposViewModel
import com.thomaskioko.stargazer.core.ViewStateResult
import com.thomaskioko.stargazer.navigation.NavigationScreen.RepoDetailScreen
import com.thomaskioko.stargazer.navigation.ScreenNavigator
import com.thomaskioko.stargazers.ui.extensions.hideView
import com.thomaskioko.stargazers.ui.extensions.showView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkedReposFragment : Fragment() {

    @Inject
    lateinit var screenNavigator: ScreenNavigator

    private val getRepoViewModel: GetBookmarkedReposViewModel by viewModels()

    private var _binding: FragmentBookmarkedReposBinding? = null
    private val binding get() = _binding!!
    private lateinit var repoListAdapter: BookmarkedReposAdapter

    private val onRepoItemClick = object : BookmarkRepoItemClick {
        override fun onClick(view: View, repoId: Long) {
            val transitionName = getString(R.string.repo_card_detail_transition_name)
            val extras = FragmentNavigatorExtras(view to transitionName)

            screenNavigator.goToScreen(RepoDetailScreen(repoId, extras))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkedReposBinding.inflate(inflater, container, false)
            .apply {
                viewmodel = getRepoViewModel
                repoList.apply {
                    repoListAdapter = BookmarkedReposAdapter(onRepoItemClick)
                    adapter = repoListAdapter
                }
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            repoList.apply {
                repoListAdapter = BookmarkedReposAdapter(onRepoItemClick)
                adapter = repoListAdapter
            }
        }

        getRepoViewModel.getBookmarkedRepos()
        getRepoViewModel.bookmarkedList
            .onEach(::handleResult)
            .launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun handleResult(viewStateResult: ViewStateResult<List<RepoViewDataModel>>) {
        when (viewStateResult) {
            is ViewStateResult.Success -> {
                with(binding) {
                    loadingBar.hideView()

                    if (viewStateResult.data.isEmpty()) {
                        tvInfo.showView()
                        tvInfo.text = getString(R.string.empty_list)
                    } else {
                        repoListAdapter.apply {
                            itemsList = viewStateResult.data
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            is ViewStateResult.Loading -> binding.loadingBar.visibility = View.VISIBLE
            is ViewStateResult.Error -> {
                Timber.e(viewStateResult.message)

                with(binding) {
                    loadingBar.hideView()
                    tvInfo.showView()
                    tvInfo.text = viewStateResult.message
                }
            }
        }
    }
}
