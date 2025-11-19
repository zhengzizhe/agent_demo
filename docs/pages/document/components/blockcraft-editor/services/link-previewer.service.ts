import { BlockCraftError, DocLinkPreviewerService, ErrorCode, isAbortError, LinkPreviewData } from '@ccc/blockcraft';
import { Injectable } from '@angular/core';

export type LinkPreviewResponseData = {
    url: string;
    title?: string;
    logo: {
        url: string;
    }
    image: {
        url: string;
    }
    description?: string;
};

@Injectable()
export class CasesDocLinkPreviewerService extends DocLinkPreviewerService {

    query = async (
        url: string,
        signal?: AbortSignal
    ): Promise<Partial<LinkPreviewData>> => {
        // if (
        //   (url.startsWith('https://x.com/') ||
        //     url.startsWith('https://www.x.com/') ||
        //     url.startsWith('https://www.twitter.com/') ||
        //     url.startsWith('https://twitter.com/')) &&
        //   url.includes('/status/')
        // ) {
        //   // use api.fxtwitter.com
        //   url =
        //     'https://api.fxtwitter.com/status/' + /\/status\/(.*)/.exec(url)?.[1];
        //   try {
        //     const { tweet } = await fetch(url, { signal }).then(res => res.json());
        //     return {
        //       title: tweet.author.name,
        //       icon: tweet.author.avatar_url,
        //       description: tweet.text,
        //       image: tweet.media?.photos?.[0].url || tweet.author.banner_url,
        //     };
        //   } catch (e) {
        //     console.error(`Failed to fetch tweet: ${url}`);
        //     console.error(e);
        //     return {};
        //   }
        // } else {
        const response = await fetch('https://api.microlink.io/?url=' + url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            signal
        })
            .then(r => {
                if (!r || !r.ok) {
                    throw new BlockCraftError(
                        ErrorCode.DefaultRuntimeError,
                        `Failed to fetch link preview: ${url}`
                    );
                }
                return r;
            })
            .catch(err => {
                if (isAbortError(err)) return null;
                console.error(`Failed to fetch link preview: ${url}`);
                console.error(err);
                return null;
            });

        if (!response) return {};

        const data: LinkPreviewResponseData = (await response.json()).data
        return {
            title: data.title,
            description: data.description,
            icon: data.logo?.url,
            image: data.image?.url
        };
        // }
    };

}
