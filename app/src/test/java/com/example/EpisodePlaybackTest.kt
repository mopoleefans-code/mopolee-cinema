package com.example

import com.example.data.FilmRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodePlaybackTest {

    @Test
    fun testEpisodeIdsAndUrlsAreUnique() {
        val episodes = FilmRepository.notldEpisodes

        // Verify all 30+ episodes exist
        assertTrue("Episodes list should contain at least 30 episodes", episodes.size >= 30)

        // Verify every episode has its own unique ID and URL
        val ids = episodes.map { it.id }
        assertEquals("Each episode must have a unique ID", ids.size, ids.distinct().size)

        val urls = episodes.map { it.videoUrl }
        assertEquals("Each episode must have a unique videoUrl (no duplicates)", urls.size, urls.distinct().size)

        // Verify Episode 1, 2, 3, 10, 20, 30 specifically
        val ep1 = episodes.find { it.episodeNumber == 1 }
        val ep2 = episodes.find { it.episodeNumber == 2 }
        val ep3 = episodes.find { it.episodeNumber == 3 }
        val ep10 = episodes.find { it.episodeNumber == 10 }
        val ep20 = episodes.find { it.episodeNumber == 20 }
        val ep30 = episodes.find { it.episodeNumber == 30 }

        assertNotNull("Episode 1 must exist", ep1)
        assertNotNull("Episode 2 must exist", ep2)
        assertNotNull("Episode 3 must exist", ep3)
        assertNotNull("Episode 10 must exist", ep10)
        assertNotNull("Episode 20 must exist", ep20)
        assertNotNull("Episode 30 must exist", ep30)

        // Ensure Episode 1's URL is distinct from Episode 2, 3, 10, 20, 30
        assertNotEquals(ep1!!.videoUrl, ep2!!.videoUrl)
        assertNotEquals(ep1.videoUrl, ep3!!.videoUrl)
        assertNotEquals(ep1.videoUrl, ep10!!.videoUrl)
        assertNotEquals(ep1.videoUrl, ep20!!.videoUrl)
        assertNotEquals(ep1.videoUrl, ep30!!.videoUrl)

        // Verify episode numbers match correctly
        assertEquals(1, ep1.episodeNumber)
        assertEquals(2, ep2.episodeNumber)
        assertEquals(3, ep3.episodeNumber)
        assertEquals(10, ep10.episodeNumber)
        assertEquals(20, ep20.episodeNumber)
        assertEquals(30, ep30.episodeNumber)

        // Verify IDs
        assertEquals("notld_ep_1", ep1.id)
        assertEquals("notld_ep_2", ep2.id)
        assertEquals("notld_ep_3", ep3.id)
        assertEquals("notld_ep_10", ep10.id)
        assertEquals("notld_ep_20", ep20.id)
        assertEquals("notld_ep_30", ep30.id)

        // Verify URLs follow independent episode schemes
        assertEquals("episode://notld_ep_1", ep1.videoUrl)
        assertEquals("episode://notld_ep_2", ep2.videoUrl)
        assertEquals("episode://notld_ep_3", ep3.videoUrl)
        assertEquals("episode://notld_ep_10", ep10.videoUrl)
        assertEquals("episode://notld_ep_20", ep20.videoUrl)
        assertEquals("episode://notld_ep_30", ep30.videoUrl)

        // Verify no fallbackUrl is pointing to Episode 1
        assertEquals("", ep1.fallbackUrl)
        assertEquals("", ep2.fallbackUrl)
        assertEquals("", ep3.fallbackUrl)
        assertEquals("", ep10.fallbackUrl)
        assertEquals("", ep20.fallbackUrl)
        assertEquals("", ep30.fallbackUrl)
    }

    @Test
    fun testOtherFilmsDoNotPointToEpisodeOne() {
        val allFilms = FilmRepository.initialFilms
        val bigBuckBunny = allFilms.find { it.id == "film_1" }
        val sintel = allFilms.find { it.id == "film_2" }
        val tearsOfSteel = allFilms.find { it.id == "film_3" }
        val gaming = allFilms.find { it.id == "film_gaming_championship" }

        assertNotNull("Big Buck Bunny must exist", bigBuckBunny)
        assertNotNull("Sintel must exist", sintel)
        assertNotNull("Tears of Steel must exist", tearsOfSteel)
        assertNotNull("Gaming Championship must exist", gaming)

        // NONE of these other films should point to night_living_dead_clip!
        assertTrue("Big Buck Bunny videoUrl must not contain night_living_dead", !bigBuckBunny!!.videoUrl.contains("night_living_dead"))
        assertTrue("Sintel videoUrl must not contain night_living_dead", !sintel!!.videoUrl.contains("night_living_dead"))
        assertTrue("Tears of Steel videoUrl must not contain night_living_dead", !tearsOfSteel!!.videoUrl.contains("night_living_dead"))
        assertTrue("Gaming videoUrl must not contain night_living_dead", !gaming!!.videoUrl.contains("night_living_dead"))
    }
}
