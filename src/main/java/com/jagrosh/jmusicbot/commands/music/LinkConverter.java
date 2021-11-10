package com.jagrosh.jmusicbot.commands.music;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;

public class LinkConverter {


    private SpotifyApi spotifyApi;
    private static LinkConverter instance;
    private String id;
    private String type;

    public LinkConverter(String spotifyClientId, String spotifyClientSecret) {
        try {
            initSpotify(spotifyClientId, spotifyClientSecret);
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            e.printStackTrace();
        }
        instance = this;
    }

    private void initSpotify(String spotifyClientId, String spotifyClientSecret) throws ParseException, SpotifyWebApiException, IOException {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .build();

        ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials creds = request.grant_type("client_credentials").build().execute();
        spotifyApi.setAccessToken(creds.getAccessToken());
    }

    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        String[] firstSplit = link.split("/");
        String[] secondSplit;

        if(firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            this.type = firstSplit[5];
        }
        else {
            secondSplit = firstSplit[4].split("\\?");
            this.type = firstSplit[3];
        }
        this.id = secondSplit[0];
        ArrayList<String> listOfTracks = new ArrayList<>();

        if(type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if(type.contentEquals("playlist")) {
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(id).build();
            Playlist playlist = playlistRequest.execute();
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                String trackID = track.getId();
                listOfTracks.add(getArtistAndName(trackID));
            }

            return listOfTracks;
        }

        return null;
    }

    private String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        String artistNameAndTrackName = "";
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();

        Track track = trackRequest.execute();
        artistNameAndTrackName = track.getName() + " - ";

        ArtistSimplified[] artists = track.getArtists();
        for(ArtistSimplified i : artists) {
            artistNameAndTrackName += i.getName() + " ";
        }

        return artistNameAndTrackName;
    }

    public static LinkConverter getInstance() {
        return instance;
    }
}
