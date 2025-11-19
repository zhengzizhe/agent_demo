export const setDocFontSize = (size: string) => {
    switch (size) {
        case 'smaller':
            document.body.style.setProperty('--bc-fs', '12px');
            document.body.style.setProperty('--bc-lh', '17px');
            document.body.style.setProperty('--bc-segments-gap', '6px');
            break;
        case 'small':
            document.body.style.setProperty('--bc-fs', '14px');
            document.body.style.setProperty('--bc-lh', '20px');
            document.body.style.setProperty('--bc-segments-gap', '8px');
            break;
        case 'default':
            document.body.style.setProperty('--bc-fs', '16px');
            document.body.style.setProperty('--bc-lh', '24px');
            document.body.style.setProperty('--bc-segments-gap', '8px');
            break;
        case 'large':
            document.body.style.setProperty('--bc-fs', '18px');
            document.body.style.setProperty('--bc-lh', '28px');
            document.body.style.setProperty('--bc-segments-gap', '10px');
            break;
        case 'larger':
            document.body.style.setProperty('--bc-fs', '22px');
            document.body.style.setProperty('--bc-lh', '34px');
            document.body.style.setProperty('--bc-segments-gap', '12px');
            break;
    }
}

