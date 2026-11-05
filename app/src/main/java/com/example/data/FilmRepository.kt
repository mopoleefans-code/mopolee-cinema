package com.example.data

object FilmRepository {
    private const val rawClipUri = "local://raw/night_living_dead_clip"
    private const val fallbackClipUri = "local://raw/mopolee_feature_film"

    private val notldEpisodesData = listOf(
        "The Cemetery at Dusk - Barbra and Johnny arrive at the lonely rural Pennsylvania cemetery to place flowers on their father's grave.",
        "They're Coming to Get You, Barbra - Johnny taunts Barbra with childhood ghost stories until a pale, staggering man approaches between the tombstones.",
        "The First Attack - Johnny struggles with the ghoul and is thrown against a gravestone; Barbra flees in terror to the car.",
        "Escape Into the Woods - Unable to find car keys, Barbra runs down the dirt road through the eerie countryside as shadows pursue her.",
        "The Isolated Farmhouse - Barbra reaches an apparently abandoned two-story rural farmhouse and seeks shelter inside.",
        "Grisly Discovery Upstairs - Searching the dark upstairs corridor, Barbra stumbles upon a mutilated corpse and recoils in horror.",
        "Ben Arrives in the Pickup Truck - A resourceful young man named Ben bursts through the front door, wielding a tire iron and driving back the walking dead.",
        "Barricading the Front Doors - Ben rips down wooden doors and boards up the front windows while Barbra sits in deep shock on the couch.",
        "The Torch and the Gasoline - Ben discovers that the reanimated ghouls are afraid of open flames and constructs makeshift torches from curtains.",
        "The Radio Broadcast - Ben turns on an antique tube radio and hears alarming news: a nationwide epidemic of mass murders and unprovoked attacks.",
        "Voices in the Cellar - Ben discovers that five other survivors have been hiding silently in the fruit cellar below the floor.",
        "Harry Cooper Steps Up - Angry middle-aged father Harry Cooper emerges, arguing furiously that the locked cellar is the only safe shelter.",
        "Helen and Karen's Secret - Harry's wife Helen reveals that their young daughter Karen was bitten on the arm and lies feverish on a cellar cot.",
        "Tom and Judy Join the Fight - Young local lovers Tom and Judy help Ben reinforce the downstairs defenses against the growing mob outside.",
        "The Emergency Television Broadcast - The group finds a television upstairs and watches emergency federal broadcasts explaining radiation from a Venus space probe.",
        "The Escape Plan - Ben conceives a daring plan: refuel the pickup truck at the farm gas pump and drive everyone to the nearest rescue shelter.",
        "The Molotov Cocktails - Helen helps prepare kerosene bottles and lit rags to throw from the upstairs bedroom window to clear the yard.",
        "Dash to the Gas Pump - Ben and Tom race outside with torches and the truck keys, clearing a fiery path through the relentless ghouls.",
        "The Padlock and the Spill - Tom accidentally drops burning kerosene while struggling to unlock the gas pump, igniting the fuel hose.",
        "The Truck Explosion - Tom and Judy attempt to drive the burning truck away from the house, but the gas tank explodes in a colossal fireball.",
        "Feast of the Dead - The ghoulish mob swarms the burning truck wreckage, leaving Ben to fight his way back toward the house alone.",
        "Harry Locks the Door - In panic and cowardice, Harry Cooper refuses to unlock the door for Ben as the walking dead close in.",
        "Ben Kicks Through - Ben breaks through the door, overpowers Harry, and re-secures the breached entrance amidst fierce anger.",
        "The Siege Begins - Dozens of decaying hands reach through splintered wooden boards, tearing at the barricades from every direction.",
        "Gunfire in the Parlor - Harry tries to snatch Ben's rifle during the chaotic struggle, leading to a fatal scuffle.",
        "The Cellar Tragedy - Down in the basement, young Karen succumbs to the infected bite and reanimates in darkness.",
        "Helen's Final Moment - Helen rushes downstairs only to be horrified by what her transformed daughter has become.",
        "Barbra's Tragic End - The front door collapses inward, and in the surging horde, Barbra sees Johnny among the undead.",
        "Ben Retreats to the Cellar - The sole survivor on the first floor, Ben shoots the reanimated attackers and bars the heavy cellar door.",
        "Night of Horrors Underground - Ben spends the agonizing final hours of the night in total darkness as ghouls scratch above the floorboards.",
        "The Dawn Search Party - Morning breaks over the Pennsylvania countryside as armed sheriff deputies and hounds comb the fields.",
        "The Final Morning Dawns - Ben hears dogs barking and climbs up into the sunlight, bringing George A. Romero's iconic horror masterpiece to its climax."
    )

    val notldEpisodes: List<Film> = notldEpisodesData.mapIndexed { index, raw ->
        val epNum = index + 1
        val parts = raw.split(" - ", limit = 2)
        val epTitle = parts.firstOrNull() ?: "Episode $epNum"
        val epSynopsis = parts.getOrNull(1) ?: raw

        Film(
            id = "notld_ep_$epNum",
            title = "Night of the Living Dead: Ep. $epNum - $epTitle",
            tagline = "Episode $epNum of 32: Classic 1968 Horror Remastered.",
            synopsis = epSynopsis,
            videoUrl = "episode://notld_ep_$epNum",
            fallbackUrl = "",
            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200&auto=format&fit=crop&q=80",
            durationMinutes = 3,
            releaseYear = 1968,
            genre = "Night of the Living Dead",
            director = "George A. Romero",
            rating = 9.4f,
            seriesTitle = "Night of the Living Dead",
            episodeNumber = epNum
        )
    }

    private val otherFilms: List<Film> = listOf(
        Film(
            id = "film_1",
            title = "Big Buck Bunny",
            tagline = "A large and lovable rabbit takes a stand against forest bullies.",
            synopsis = "A newly awakened giant rabbit, Big Buck Bunny, has his peaceful woodland morning disrupted by forest bullies. A heartwarming and comedic animated adventure.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            fallbackUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            durationMinutes = 10,
            releaseYear = 2023,
            genre = "Animation",
            director = "Sacha Goedegebure",
            rating = 8.8f
        ),
        Film(
            id = "film_2",
            title = "Sintel",
            tagline = "A lonely young woman searches the desert for her lost dragon companion.",
            synopsis = "Sintel is a fierce traveler who nurses an injured baby dragon back to health, only to have it snatched by a ferocious adult dragon. Her quest tests her resilience and sorrow.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            fallbackUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            posterUrl = "https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?w=800&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            durationMinutes = 15,
            releaseYear = 2023,
            genre = "Adventure",
            director = "Colin Levy",
            rating = 9.1f
        ),
        Film(
            id = "film_3",
            title = "Tears of Steel",
            tagline = "In a dystopian future, love and robotics collide in Amsterdam.",
            synopsis = "Set in a dystopian future in Amsterdam, a group of warriors and scientists try to save the world from destructive robots using neural memory technology.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            fallbackUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&auto=format&fit=crop&q=80",
            durationMinutes = 12,
            releaseYear = 2024,
            genre = "Sci-Fi",
            director = "Ian Hubert",
            rating = 8.9f
        ),
        Film(
            id = "film_gaming_championship",
            title = "Apex & Cyberpunk Gaming Championship",
            tagline = "Next-gen cyber battles, esports action and futuristic gameplay thrills.",
            synopsis = "Enter the high-octane world of competitive esports and futuristic gaming. Featuring cinematic 60fps action, tactical team clashes, and glowing cyber arenas presented in big screen mode.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            fallbackUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&auto=format&fit=crop&q=80",
            durationMinutes = 14,
            releaseYear = 2026,
            genre = "Gaming & Action",
            director = "MOPOLEE Gaming Studio",
            rating = 9.8f
        )
    )

    val initialFilms: List<Film> = notldEpisodes + otherFilms

    val categories: List<String> = listOf(
        "All",
        "Night of the Living Dead",
        "Gaming & Action",
        "Animation",
        "Adventure",
        "Sci-Fi",
        "My Uploads"
    )
}
