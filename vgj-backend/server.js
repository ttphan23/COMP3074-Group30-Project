require("dotenv").config();
const express = require("express");
const axios = require("axios");
const cors = require("cors");

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Helper: get IGDB access token via Twitch
async function getAccessToken() {
  const clientId = process.env.TWITCH_CLIENT_ID;
  const clientSecret = process.env.TWITCH_CLIENT_SECRET;

  const res = await axios.post(
    "https://id.twitch.tv/oauth2/token",
    null,
    {
      params: {
        client_id: clientId,
        client_secret: clientSecret,
        grant_type: "client_credentials",
      },
    }
  );

  return res.data.access_token;
}

// Route: /games/trending
app.get("/games/trending", async (req, res) => {
  try {
    const accessToken = await getAccessToken();

    const igdbRes = await axios.post(
      `${process.env.IGDB_BASE_URL}/games`,
      // IGDB query language body:
      `
        fields name, summary, total_rating;
        sort total_rating desc;
        where total_rating != null;
        limit 10;
      `,
      {
        headers: {
          "Client-ID": process.env.TWITCH_CLIENT_ID,
          Authorization: `Bearer ${accessToken}`,
        },
      }
    );

    const games = igdbRes.data || [];

    // Map IGDB response to your GameApiModel shape
    const simplified = games.map((g) => ({
      id: g.id,
      title: g.name || "Unknown Game",
      description: g.summary || "",
      rating: g.total_rating || 0,
    }));

    res.json(simplified);
  } catch (err) {
    console.error("Error fetching IGDB games:", err.response?.data || err.message);
    // Fallback dummy data so app still works:
    res.json([
  {
    id: 1,
    title: "Elden Ring",
    description: "Epic open-world action RPG.",
    rating: 4.9,
  },
  {
    id: 2,
    title: "Baldur's Gate 3",
    description: "Deep narrative-driven RPG.",
    rating: 4.8,
  },
  {
    id: 3,
    title: "Hades",
    description: "Fast-paced roguelike with amazing combat.",
    rating: 4.7,
  },
  {
    id: 4,
    title: "Cyberpunk 2077",
    description: "Open-world sci-fi RPG — now fully fixed.",
    rating: 4.6,
  },
  {
    id: 5,
    title: "The Witcher 3",
    description: "Legendary story-driven RPG.",
    rating: 4.9,
  },
  {
    id: 6,
    title: "Hollow Knight",
    description: "Metroidvania masterpiece.",
    rating: 4.8,
  },
  {
    id: 7,
    title: "Final Fantasy XVI",
    description: "Cinematic, fast-paced action RPG.",
    rating: 4.5,
  },
  {
    id: 8,
    title: "Sekiro: Shadows Die Twice",
    description: "Brutally satisfying combat challenge.",
    rating: 4.7,
  }
  ]);
  }
});

app.listen(PORT, () => {
  console.log(`VGJ backend running on http://localhost:${PORT}`);
});
